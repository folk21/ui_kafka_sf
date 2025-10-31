#!/usr/bin/env bash 

zip -r archive.zip . -x ".idea/*" ".git/*" ".gradle/*" "build/*" "*/build/*" "node_modules/*" "*/node_modules/*" "*.class" "package-lock.json" "*.jar" "*.zip"
