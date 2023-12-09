package org.windr.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/brands")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class BrandResource {

    @GET
    public List<Brand> get() {
        return Brand.listAll();
    }

    @GET
    @Path("{id}")
    public Brand getBrand(Long id) {
        return Brand.findById(id);
    }

    @POST
    @Transactional
    @Produces("application/json")
    @Consumes("application/json")
    public Response create(Brand brand) {
        brand.persist();
        return Response.ok(brand).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, Brand brand) {
        Brand existingBrand = Brand.findById(id);
        if (existingBrand == null) {
            throw new WebApplicationException("Brand with id of " + id + " does not exist.", 404);
        }
        existingBrand.name = brand.name;
        existingBrand.description = brand.description;
        existingBrand.logo = brand.logo;
        existingBrand.persist();
        return Response.ok(existingBrand).status(200).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        Brand brand = Brand.findById(id);
        if (brand == null) {
            throw new WebApplicationException("Brand with id of " + id + " does not exist.", 404);
        }
        brand.delete();
        return Response.status(204).build();
    }



}
