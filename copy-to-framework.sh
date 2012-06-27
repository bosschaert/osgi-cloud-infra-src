VERSION=1.0.0-SNAPSHOT
TARGET=$1

function copy {
  MODULE="$1"
  BUNDLE="$MODULE-$VERSION.jar"
  cp $MODULE/target/$BUNDLE $TARGET/osgi/equinox/bundles
  echo "bundles/$BUNDLE@start, \\" >> $TARGET/osgi/equinox/config-template/config.ini  
}

if [ ! -e $TARGET/osgi/equinox ] 
then
    echo Directory structure not as expected in $TARGET
    echo Expected at least an osgi/equinox directory in that location
    exit
fi

cp $TARGET/osgi/equinox/config-template/config.ini.base $TARGET/osgi/equinox/config-template/config.ini

copy cloud-infra-framework-service
copy cloud-infra-services
copy cloud-infra-zookeeper-plugin

echo " " >> $TARGET/osgi/equinox/config-template/config.ini  
