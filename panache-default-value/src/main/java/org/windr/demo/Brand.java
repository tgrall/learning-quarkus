package org.windr.demo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table (
    name = "brands"
)
public class Brand extends PanacheEntity {

    public String name;
    public String description;

    @Column(columnDefinition = "varchar(255) default 'default-logo.png'")
    public String logo = "default-logo.png";

}
