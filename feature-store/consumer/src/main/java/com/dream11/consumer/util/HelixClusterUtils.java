package com.dream11.consumer.util;

import com.dream11.common.app.AppContext;
import com.dream11.common.guice.DefaultModule;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.consumer.MainModule;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.HelixConfig;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.model.OnlineOfflineSMD;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import java.util.List;

@Slf4j
public class HelixClusterUtils {

  public static void main(String[] args){
    if(args.length == 0)
      throw new RuntimeException("args cannot be empty");

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
    config.setClusterName(applicationConfig.getConsumerHelixClusterName());
    ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(config.getZkServer());

    switch (args[0]){
      case ("create-cluster"):
        zkHelixAdmin.addCluster(config.getClusterName());
        zkHelixAdmin.addStateModelDef(
            config.getClusterName(), OnlineOfflineSMD.name, new OnlineOfflineSMD());
        log.info(String.format("cluster created with name: %s", config.getClusterName()));
        break;
      default:
        throw new RuntimeException(String.format("functionality not implemented for %s", args[0]));
    }
    zkHelixAdmin.close();
  }
}
