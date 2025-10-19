#!/usr/bin/env python3
"""
Scan XML files and remove any bytes before the first '<' (this removes BOM or stray characters
that cause "Content is not allowed in prolog" errors). Creates .bak backups before modifying files.
Usage: python tools/check_and_fix_xml_prolog.py

คำอธิบายภาษาไทย:
- สคริปต์นี้จะสแกนไฟล์ XML ภายในโฟลเดอร์โปรเจกต์ (เจาะจงที่ `app/src/main`)
- ถ้าพบว่ามีไบต์ที่อยู่ก่อนเครื่องหมาย '<' ตัวแรก (เช่น BOM หรืออักขระหลุดมา) จะถือว่าเป็นปัญหา
- สคริปต์จะสร้างสำเนาแบ็กอัพไฟล์ (นามสกุล .bak) แล้วตัดส่วนก่อน '<' ทิ้ง เพื่อให้ไฟล์เริ่มด้วย '<' โดยตรง
- เหมาะสำหรับแก้ปัญหา "Content is not allowed in prolog" ของไฟล์ XML
"""
import sys
from pathlib import Path

# ROOT ชี้ไปยังรากของโปรเจกต์ (ปรับเป็นพาธของ workspace)
ROOT = Path('c:/Users/RINDARUN/AndroidStudioProjects/mini-project')

# คำแปล: patterns คือรายการพาธ (glob) ที่จะสแกนหาไฟล์ XML
# - เราสนใจไฟล์ XML ทั้งหมดภายใต้ app/src/main และ AndroidManifest.xml
patterns = [
    'app/src/main/**/*.xml',
    'app/src/main/AndroidManifest.xml'
]

# เก็บไฟล์ที่พบใน set เพื่อหลีกเลี่ยงการซ้ำ
files = set()
for p in patterns:
    # ROOT.glob จะคืนค่าไฟล์ตาม pattern ที่กำหนด
    for f in ROOT.glob(p):
        if f.is_file():
            files.add(f)

# problems จะเก็บ (path, leading_bytes) ของไฟล์ที่มีไบต์ก่อน '<'
problems = []
fixed = []
for f in sorted(files):
    try:
        b = f.read_bytes()
    except Exception as e:
        # อ่านไฟล์ไม่ได้ ให้ข้ามและพิมพ์ error
        print(f"[ERROR] cannot read {f}: {e}")
        continue
    if not b:
        # ไฟล์ว่าง -> ข้าม
        continue
    # หาตำแหน่งไบต์ตัวแรกที่เป็น '<'
    idx = b.find(b'<')
    # pre คือไบต์ก่อนตำแหน่ง '<' (ถ้ามี)
    pre = b[:idx] if idx >= 0 else b

    # กรณี idx <= 0 หมายถึง:
    # - idx == 0 -> ไฟล์เริ่มด้วย '<' อยู่แล้ว (ปกติ)
    # - idx == -1 -> ไม่พบ '<' เลย (ไฟล์ไม่น่าจะเป็น XML)
    if idx <= 0:
        if idx == 0:
            # เริ่มด้วย '<' อยู่แล้ว -> ไม่มีปัญหา
            continue
        else:
            # ไม่พบ '<' เลย ให้เตือนแล้วข้าม
            print(f"[WARN] no '<' found in {f}")
            continue
    # ถ้ามาถึงจุดนี้ แปลว่ามีไบต์ก่อน '<' -> เก็บไว้เป็นปัญหา
    problems.append((f, pre))

# ถ้าไม่มีปัญหาให้จบโปรแกรม
if not problems:
    print("No XML prolog problems detected.")
    sys.exit(0)

# รายงานไฟล์ที่พบปัญหา (แสดง preview ของไบต์นำหน้าเป็น hex)
print(f"Found {len(problems)} XML files with content before '<' (will create .bak and fix):")
for f, pre in problems:
    # แสดง hex สั้นๆ ของไบต์นำหน้า เพื่อช่วยวิเคราะห์ (เช่น BOM = ef bb bf)
    hex_preview = ' '.join(f'{x:02x}' for x in pre[:16]) + (" ..." if len(pre) > 16 else "")
    print(f" - {f}  (leading bytes: {hex_preview})")

# เริ่มแก้ไขไฟล์ทีละไฟล์
for f, pre in problems:
    try:
        orig = f.read_bytes()
        idx = orig.find(b'<')
        if idx <= 0:
            # ถ้ามีเหตุผลใดๆ ให้ข้าม (shouldn't happen เพราะเราตรวจก่อนหน้าแล้ว)
            continue
        # สร้างไฟล์แบ็กอัพ: same folder, ชื่อเดิม + .bak
        # ตัวอย่าง: values.xml จะมีแบ็กอัพชื่อ values.xml.bak
        bak_path = f.parent / (f.name + '.bak')
        if not bak_path.exists():
            bak_path.write_bytes(orig)
        else:
            # หากแบ็กอัพมีอยู่แล้ว จะไม่เขียนทับ เพื่อความปลอดภัย
            pass
        # ตัดส่วนไบต์ก่อน '<' ทิ้ง และเขียนทับไฟล์ต้นฉบับ
        newb = orig[idx:]
        f.write_bytes(newb)
        fixed.append(f)
        print(f"Fixed {f} (removed {idx} leading bytes)")
    except Exception as e:
        print(f"[ERROR] failed to fix {f}: {e}")

print(f"Done. Fixed {len(fixed)} files. Backups created alongside originals with .bak suffix.")
