#!/bin/bash
sbt clean; sbt dist || exit 1
scp target/universal/railinfo-1.0-SNAPSHOT.zip railinfo.kabelverhau.ch: || exit 1
ssh railinfo.kabelverhau.ch 'sudo systemctl stop railinfo; rm -rf railinfo-1.0-SNAPSHOT; unzip railinfo-1.0-SNAPSHOT.zip; sudo systemctl start railinfo' || exit 1
