package org.windr.demo;

import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;

record BoardProjection(
    Long id,
    String name,
    String image,
    String program,
    @ProjectedFieldName("brand.name") String brandName
) { }