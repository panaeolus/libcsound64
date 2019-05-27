#!/usr/bin/env bash

patchelf --force-rpath --set-rpath '$ORIGIN' ./libcsound64.so.6.0
patchelf --set-soname libcsound64.so ./libcsound64.so.6.0
mv libcsound64.so.6.0 libcsound64.so

mv mv libsndfile.so.1.0.28 libsndfile.so.1
patchelf --force-rpath --set-rpath '$ORIGIN' ./libsndfile.so.1

mv libFLAC.so.8.3.0 libFLAC.so.8
patchelf --force-rpath --set-rpath '$ORIGIN' ./libFLAC.so.8

mv libogg.so.0.8.3 libogg.so.0
patchelf --force-rpath --set-rpath '$ORIGIN' ./libogg.so.0

mv libvorbisenc.so.2.0.11 libvorbisenc.so.2
patchelf --force-rpath --set-rpath '$ORIGIN' ./libvorbisenc.so.2

mv libvorbis.so.0.4.8 libvorbis.so.0
patchelf --force-rpath --set-rpath '$ORIGIN' ./libvorbis.so.0
