# -*- mode: python -*-

block_cipher = None

a = Analysis(['standfire_mini_interface.py'],
             pathex=['C:\\Users\\bhdavis\\Documents\\STANDFIRE\\source\\standfire',
             'C:\\Users\\bhdavis\\Documents\\STANDFIRE\\source\\apps\\STANDFIRE_v1.0'],
             binaries=[],
             datas=[],
             hiddenimports=[],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          exclude_binaries=True,
          name='standfire_mini_interface',
          debug=False,
          strip=False,
          upx=True,
          console=True,
          icon='sf_icon_64.ico')
coll = COLLECT(exe,
               a.binaries,
               a.zipfiles,
               a.datas,
               strip=False,
               upx=True,
               name='standfire_mini_interface')
