package dev.parodos.assessment;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/infrastructures")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class InfrastructureResource {
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureResource.class);

    private final Set<Infrastructure> infrastructures = Collections.synchronizedSet(new LinkedHashSet<>());

    @POST
    public Response assess(@QueryParam("repo") String repo) {
        logger.info("Repo being assessed: {}", repo);
        if (!repo.contains("java")) {
            infrastructures.add( new Infrastructure ("migrationAnalysis", "Migration Analysis", "Migration analysis description"));
            return Response.ok(infrastructures).build();
        }
        infrastructures.add( new Infrastructure ("move2Kube", "Move2Kube", "Move2Kube description"));
        infrastructures.add( new Infrastructure ("ocpOnboarding", "Ocp Onboarding", "Ocp onboarding description"));
        infrastructures.add( new Infrastructure ("vmOnboarding", "VM Onboarding", "VM onboarding description"));
        return Response.ok(infrastructures).build();
    }

}