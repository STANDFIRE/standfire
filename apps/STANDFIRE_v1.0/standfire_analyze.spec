# -*- mode: python -*-
a = Analysis(['standfire_analyze.py'],
             pathex=['C:\\Users\\wellslu\\Projects\\standfire\\repo\\standfire\\apps\\STANDFIRE_v1.0'],
             hiddenimports=[],
             hookspath=None,
             runtime_hooks=None)
pyz = PYZ(a.pure)
exe = EXE(pyz,
          a.scripts,
          exclude_binaries=True,
          name='standfire_analyze.exe',
          debug=False,
          strip=None,
          upx=True,
          console=True , icon='sf_icon_64.ico')
coll = COLLECT(exe,
               a.binaries,
               a.zipfiles,
               a.datas,
               strip=None,
               upx=True,
               name='standfire_analyze')
