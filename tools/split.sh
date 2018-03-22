#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ROOT="$DIR/.."

INPUT="$ROOT/RSBot-7062.jar"
CLASSES="deob/classes"
SOURCES="deob/sources"

for f in `find $CLASSES -name '*.class'`; do
    OUTPUT_FILE="${f/$CLASSES/$SOURCES}"
    OUTPUT_FILE="${OUTPUT_FILE::-6}.java"

    if [ -f "$OUTPUT_FILE" ]; then
        if [ "$OUTPUT_FILE" -nt "$f" ]; then
            echo "Unmodified: $f"
            continue
        fi
    fi

    mkdir -p `dirname $OUTPUT_FILE`
    echo "Decompiling: $f"
    java -jar "$ROOT/tools/cfr_0_125.jar" "$f" > $OUTPUT_FILE
done;
