#!/usr/bin/env bash
for i in {1..5}
do
   echo "Cycle $i starting..."
   ./target/release/gha advance
done
