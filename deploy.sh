#!/bin/bash
sbt clean; sbt dist || exit 1
#scp -o ProxyJump=david@backup0.kabelverhau.ch target/universal/railinfo-1.0-SNAPSHOT.zip railinfo.kabelverhau.ch: || exit 1
#ssh railinfo.kabelverhau.ch -J backup0.kabelverhau.ch 'sudo systemctl stop railinfo; rm -rf railinfo-1.0-SNAPSHOT; unzip railinfo-1.0-SNAPSHOT.zip; sudo systemctl start railinfo' || exit 1

host="railinfo.kabelverhau.ch"
rsync -v -e "ssh -J backup0.kabelverhau.ch" --rsync-path "sudo -u railinfo rsync" target/universal/railinfo-1.0-SNAPSHOT.zip "${host}:/home/railinfo/" || exit 1
ssh "${host}" -J backup0.kabelverhau.ch "sudo systemctl stop railinfo"
ssh "${host}" -J backup0.kabelverhau.ch "sudo -u railinfo -i sh -c 'rm -rf railinfo-1.0-SNAPSHOT; unzip railinfo-1.0-SNAPSHOT.zip'"
ssh "${host}" -J backup0.kabelverhau.ch "sudo systemctl start railinfo"
