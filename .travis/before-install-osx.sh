#!/bin/sh

export HOMEBREW_NO_ANALYTICS=1
export HOMEBREW_NO_AUTO_UPDATE=1

brew install libsndfile || brew install libsndfile || exit 1
brew install portaudio || exit 2
brew install ccache || exit 3
# we don't upgrade qt since we don't update homebrew
brew link qt5 --force || exit 5
brew install fftw || exit 6

if $USE_SYSLIBS; then
    # boost is already installed
    brew install yaml-cpp || exit 7
fi

# according to https://docs.travis-ci.com/user/caching#ccache-cache
export PATH="/usr/local/opt/ccache/libexec:$PATH"

# To get less noise in xcode output
gem install xcpretty
