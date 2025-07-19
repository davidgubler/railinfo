#!/bin/bash
sbt clean; sbt dist || exit 1
scp target/universal/railinfo-1.0-SNAPSHOT.zip railinfo.kabelverhau.ch: || exit 1
ssh railinfo.kabelverhau.ch 'sudo systemctl stop railinfo; rm -rf railinfo-1.0-SNAPSHOT; unzip railinfo-1.0-SNAPSHOT.zip; sudo systemctl start railinfo' || exit 1

host="railinfo.kabelverhau.ch"
rsync --rsync-path "sudo -u railinfo rsync" target/universal/railinfo-1.0-SNAPSHOT.zip "${host}:/home/railinfo/" || exit 1
ssh "${host}" "sudo systemctl stop railinfo"
ssh "${host}" "sudo -u railinfo -i sh -c 'rm -rf railinfo-1.0-SNAPSHOT; unzip railinfo-1.0-SNAPSHOT.zip'"
ssh "${host}" "sudo systemctl start railinfo"
