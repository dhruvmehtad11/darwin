package darwincatalog.service.work;

import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Component;

@Component
public class WorkExecutor {
  private final EntityManager entityManager;

  public WorkExecutor(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void execute(Work work) {
    Session session = entityManager.unwrap(Session.class);
    session.doWork(work);
  }
}
