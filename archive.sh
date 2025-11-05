#!/usr/bin/env bash 

zip -r archive.zip . -x ".idea/*" ".git/*" ".gradle/*" "build/*" "*/build/*" "*/node_modules/*" "*.class" "*.jar" "*.zip"
