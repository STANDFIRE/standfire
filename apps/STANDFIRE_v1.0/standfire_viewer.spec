# -*- mode: python -*-
a = Analysis(['standfire_viewer.py'],
             pathex=['C:\\Users\\wellslu\\Projects\\standfire\\repo\\standfire\\apps\\STANDFIRE_v1.0'],
             hiddenimports=['pyfvs.win.pyfvsak',
			                'pyfvs.win.pyfvsbmc',
							'pyfvs.win.pyfvscac',
							'pyfvs.win.pyfvscic',
							'pyfvs.win.pyfvscrc',
							'pyfvs.win.pyfvscs',
							'pyfvs.win.pyfvsecc',
							'pyfvs.win.pyfvsiec',
							'pyfvs.win.pyfvsktc',
							'pyfvs.win.pyfvsls',
							'pyfvs.win.pyfvsncc',
							'pyfvs.win.pyfvsne',
							'pyfvs.win.pyfvspnc',
							'pyfvs.win.pyfvssn',
							'pyfvs.win.pyfvssoc',
							'pyfvs.win.pyfvsttc',
							'pyfvs.win.pyfvsutc',
							'pyfvs.win.pyfvswcc',
							'pyfvs.win.pyfvswsc'],
             hookspath=None,
             runtime_hooks=None)
pyz = PYZ(a.pure)
exe = EXE(pyz,
          a.scripts,
          exclude_binaries=True,
          name='standfire_viewer.exe',
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
               name='standfire_viewer')
