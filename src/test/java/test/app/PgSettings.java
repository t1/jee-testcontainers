package test.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pg_settings")
@Getter @Setter
public class PgSettings {
    String setting;
    @Id String name;
}
