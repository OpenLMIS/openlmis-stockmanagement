package org.openlmis.stockmanagement.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {

  public static final String TEXT_COLUMN_DEFINITION = "text";
  public static final String PG_UUID = "pg-uuid";

  @Id
  @GeneratedValue(generator = "uuid-gen")
  @GenericGenerator(name = "uuid-gen", strategy = "uuid2")
  @Type(type = PG_UUID)
  @Getter
  @Setter
  protected UUID id;

  /*
  Create instance of base entity.
   */
  public BaseEntity() {
  }

  /**
   * Create entity with id.
   *
   * @param id    id.
   * @param clazz type of entity.
   * @param <T>   type of entity.
   * @return created entity object.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException IllegalAccessException.
   */
  public static <T extends BaseEntity> T fromId(UUID id, Class<T> clazz)
          throws IllegalAccessException, InstantiationException {
    if (id == null) {
      return null;
    }

    T instance = clazz.newInstance();
    instance.setId(id);
    return instance;
  }
}
