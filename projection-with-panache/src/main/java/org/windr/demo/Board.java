package org.windr.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table (
    name = "boards"
)
public class Board extends PanacheEntity {
        public String name;
        @ManyToOne( fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "brand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_board_brand_id"))
        public Brand brand;
        public String description;
        public String image;
        public String program;
}
