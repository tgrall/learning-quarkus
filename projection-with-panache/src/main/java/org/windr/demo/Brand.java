package org.windr.demo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "brands"
)
public class Brand extends PanacheEntity {

        public String name;
        public String description;
        public String logo;
        public String website;
        public String address;

}
