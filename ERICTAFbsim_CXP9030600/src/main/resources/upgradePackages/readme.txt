This folder is required to store upgrade packages temporarily by PreCheckUpgradePackages.java.
The upgrade packages are uploaded to SHM and then deleted. If deletion fails, the user is notified 
via the console to manually delete the packages. 
IMPORTANT: The upgrade packages cannot be pushed to the repository
IMPORTANT: Deleting this folder will break tests so DO NOT REMOVE.