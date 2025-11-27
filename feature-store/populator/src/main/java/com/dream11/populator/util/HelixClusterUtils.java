package com.dream11.populator.util;

import com.dream11.common.app.AppContext;
import com.dream11.common.guice.DefaultModule;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.HelixConfig;
import com.google.inject.AbstractModule;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.model.OnlineOfflineSMD;
import java.util.List;

@Slf4j
public class HelixClusterUtils {

  public static void main(String[] args){
    if(args.length == 0)
      throw new RuntimeException("args cannot be empty");

    switch (args[0]){
      case ("create-cluster"):
        createCluster();
        break;
      default:
        throw new RuntimeException(String.format("functionality not implemented for %s", args[0]));
    }
  }

  public static void createCluster(){
    DefaultModule defaultModule = new DefaultModule(Vertx.vertx());
    AbstractModule mainModule = new AbstractModule(){
      @Override
      protected void configure(){
        bind(ApplicationConfig.class).toProvider(ConfigManager.class);
      }
    };

    AppContext.initialize(List.of(mainModule, defaultModule));

    ApplicationConfig applicationConfig = AppContext.getInstance(ApplicationConfig.class);

    HelixConfig config = ConfigReader.readHelixConfigFromFile();
    config.setClusterName(applicationConfig.getPopulatorHelixClusterName());
    ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(config.getZkServer());

    zkHelixAdmin.addCluster(config.getClusterName());
    zkHelixAdmin.addStateModelDef(
        config.getClusterName(), OnlineOfflineSMD.name, new OnlineOfflineSMD());
    log.info(String.format("cluster created with name: %s", config.getClusterName()));
    AppContext.reset();
    zkHelixAdmin.close();
  }
}
