package test.app;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pg_settings")
@Getter @Setter
public class PgSettings {
    String setting;
    @Id String name;
}
