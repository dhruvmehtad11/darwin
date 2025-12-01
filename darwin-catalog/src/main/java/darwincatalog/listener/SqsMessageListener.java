package darwincatalog.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.config.Properties;
import darwincatalog.model.GlueDetail;
import darwincatalog.model.GlueEvent;
import darwincatalog.service.DdlSyncService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Service
@Slf4j
public class SqsMessageListener implements DdlEventListener {

  private final SqsClient sqsClient;
  private final ObjectMapper customMapper;
  private final DdlSyncService glueSyncService;
  private final Properties properties;

  public SqsMessageListener(
      SqsClient sqsClient,
      @Qualifier("lenientMapper") ObjectMapper customMapper,
      @Qualifier("glueSyncService") DdlSyncService ddlSyncService,
      Properties properties) {
    this.sqsClient = sqsClient;
    this.customMapper = customMapper;
    this.glueSyncService = ddlSyncService;
    this.properties = properties;
  }

  @Override
  public String getType() {
    return "GLUE";
  }

  public void poll() {
    if (!properties.isGlueListenerEnabled()) {
      return;
    }
    pollQueue(properties.getGlueListenerQueueUrl());
  }

  public void pollQueue(String queueUrl) {
    ReceiveMessageRequest request =
        ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(10)
            .build();

    List<Message> messages = sqsClient.receiveMessage(request).messages();
    if (CollectionUtils.isEmpty(messages)) {
      return;
    }
    log.info("Processing {} messages ", messages.size());
    for (Message message : messages) {
      try {
        String json = message.body();
        GlueEvent glueEvent = customMapper.readValue(json, GlueEvent.class);
        processEvent(glueEvent);
        sqsClient.deleteMessage(
            DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
      } catch (Exception e) {
        log.error(
            "Error while parsing glue event: {} for the message {}",
            e.getMessage(),
            message.body());
      }
    }
  }

  private void processEvent(GlueEvent glueEvent) {
    GlueDetail glueDetail = glueEvent.getDetail();
    String type = glueDetail.getTypeOfChange();
    String databaseName = glueDetail.getDatabaseName();
    List<String> affectedTables = glueDetail.getChangedTables();
    String affectedTable = glueDetail.getTableName();

    switch (type) {
      case "CreateDatabase":
      case "CreateTable":
        glueSyncService.addTables(databaseName, affectedTables);
        break;
      case "UpdateTable":
        glueSyncService.updateTable(databaseName, affectedTable);
        break;
      case "DeleteTable":
      case "DeleteDatabase":
        glueSyncService.deleteTables(databaseName, affectedTables);
        break;
      default:
        log.warn("Unknown type of change: {}", type);
    }
  }
}
