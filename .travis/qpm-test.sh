#!/bin/bash
# echo "pip version"
# pip --version
# echo "pip2 version"  
# pip2 --version
# echo "pip3 version"
# pip3 --version
QTWEBENGINE_CHROMIUM_FLAGS="--disable-gpu" $SCLANG
QPM_DEBUG=1
echo "\"hello\".postln; 0.exit" >> test.scd
$SCLANG test.scd
qpm test.run -l $TRAVIS_BUILD_DIR/BUILD/travis_test_run.json --path $SCLANG --include $HOME/Quarks
