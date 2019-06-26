# cd into a csound repo
# DELETE FROM CMAKE_LISTS
# -if(WIN32)
# -  find_library(FLAC_LIB NAMES FLAC flac)
# -  find_library(OGG_LIB ogg)
# -  find_library(SPEEX_LIB speex)
# -  find_library(VORBIS_LIB vorbis)
# -  find_library(VORBISENC_LIB vorbisenc)
# -  set(LIBSNDFILE_SUPPORT_LIBS ${VORBIS_LIB} ${VORBISENC_LIB} ${FLAC_LIB} ${OGG_LIB})
# -  if(SPEEX_LIB)c
# -    list(APPEND LIBSNDFILE_SUPPORT_LIBS ${SPEEX_LIB} )
# -  endif()
# -  #message(STATUS "${LIBSNDFILE_SUPPORT_LIBS}")
# -endif()

# rm -r build
# mkdir build
# cd build

cmake ..\ -G "Visual Studio 15 2017 Win64" -DUSE_DOUBLE=1 -DUSE_JACK=1 -DBUILD_JACK_OPCODES=0 -DCMAKE_BUILD_TYPE="Release" -DCMAKE_INSTALL_PREFIX="dist" -DJACK_LIB="C:\Users\User\Downloads\radium_64bit_windows-5.9.65-demo\radium_64bit_windows-5.9.65-demo\bin\jack_local\libjack64.lib" -DJACK_LIBRARY="C:\Users\User\Downloads\radium_64bit_windows-5.9.65-demo\radium_64bit_windows-5.9.65-demo\bin\jack_local\libjack64.lib" -DJACKDMP_LIBRARY="C:\Users\User\Downloads\radium_64bit_windows-5.9.65-demo\radium_64bit_windows-5.9.65-demo\bin\jack_local\libjack64.lib" -DJACK_HEADER="C:\users\user\appdata\local\temp\include" -DLIBSNDFILE_LIBRARY="C:\Program Files\Mega-Nerd\libsndfile\lib\libsndfile-1.lib" -DSNDFILE_H_PATH="C:\Program Files\Mega-Nerd\libsndfile\include" -DBISON_EXECUTABLE="..\..\..\Downloads\win_flex_bison-latest\win_bison.exe" -DFLEX_EXECUTABLE="C:\Users\User\Downloads\win_flex_bison-latest\win_flex.exe"
cmake --build .
# cd ..