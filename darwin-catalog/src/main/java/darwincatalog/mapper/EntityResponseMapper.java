package darwincatalog.mapper;

public interface EntityResponseMapper<E, R> {
  E toEntity(R r);

  R toDto(E e);
}
