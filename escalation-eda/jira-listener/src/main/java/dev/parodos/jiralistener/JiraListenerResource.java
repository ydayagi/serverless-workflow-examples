package dev.parodos.jiralistener;

import static dev.parodos.jiralistener.model.JiraConstants.ISSUE;
import static dev.parodos.jiralistener.model.JiraConstants.FIELDS;
import static dev.parodos.jiralistener.model.JiraConstants.KEY;
import static dev.parodos.jiralistener.model.JiraConstants.LABELS;
import static dev.parodos.jiralistener.model.JiraConstants.STATUS;
import static dev.parodos.jiralistener.model.JiraConstants.STATUS_CATEGORY;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.parodos.jiralistener.model.ClosedJiraTicket;
import dev.parodos.jiralistener.model.JiraIssue;
import dev.parodos.jiralistener.model.JiraIssue.StatusCategory;
import dev.parodos.jiralistener.model.WebhookEvent;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/")
public class JiraListenerResource {
    @ConfigProperty(name = "cloudevent.type")
    String cloudeventType;
    @ConfigProperty(name = "cloudevent.source")
    String cloudeventSource;

    @ConfigProperty(name = "jira.webhook.label.workflowInstanceId")
    String workflowInstanceIdJiraLabel;
    @ConfigProperty(name = "jira.webhook.label.workflowName")
    String workflowNameJiraLabel;
    @ConfigProperty(name = "escalation.workflowName")
    String expectedWorkflowName;

    private Logger logger = System.getLogger(JiraListenerResource.class.getName());

    @Inject
    @RestClient
    EventNotifier eventNotifier;

    @Inject
    ObjectMapper mapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public void test(Object any) {
        logger.log(Level.INFO, "RECEIVED " + any);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/webhook/jira")
    public Response onEvent(Map<String, Object> requestBody) {
        logger.log(Level.INFO, "Received " + requestBody);
        JiraIssue jiraIssue = null;
        try {
            WebhookEvent webhookEvent = mapper.readValue(mapper.writeValueAsBytes(requestBody), WebhookEvent.class);
            logger.log(Level.INFO, "Received " + webhookEvent);
            if (webhookEvent.getIssue() == null) {
                logger.log(Level.INFO, "Discarded because of missing field: " + ISSUE);
                return Response.noContent().build();
            }
            jiraIssue = webhookEvent.getIssue();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST.getStatusCode(), "Not a valid Jira issue: " + e.getMessage())
                    .build();
        }

        if (jiraIssue.getKey() != null) {
            String issueKey = jiraIssue.getKey();

            if (jiraIssue.getFields() == null) {
                logger.log(Level.INFO, "Discarded because of missing field: " + FIELDS);
                return Response.noContent().build();
            }

            if (jiraIssue.getFields().getLabels() == null) {
                logger.log(Level.INFO, String.format("Discarded because of missing field: %s.%s", FIELDS, LABELS));
                return Response.noContent().build();
            }
            List<String> labels = jiraIssue.getFields().getLabels();

            Optional<String> workflowIdLabel = labels.stream()
                    .filter(l -> l.startsWith(workflowInstanceIdJiraLabel + "=")).findFirst();
            if (workflowIdLabel.isEmpty()) {
                logger.log(Level.INFO,
                        String.format("Discarded because no %s label found", workflowInstanceIdJiraLabel));
                return Response.noContent().build();
            }
            String workflowId = workflowIdLabel.get().split("=")[1];

            Optional<String> workflowNameLabel = labels.stream()
                    .filter(l -> l.startsWith(workflowNameJiraLabel + "=")).findFirst();
            if (workflowNameLabel.isEmpty()) {
                logger.log(Level.INFO, String.format("Discarded because no %s label found", workflowNameJiraLabel));
                return Response.noContent().build();
            }
            String workflowName = workflowNameLabel.get().split("=")[1];
            if (!workflowName.equals(expectedWorkflowName)) {
                logger.log(Level.INFO,
                        String.format("Discarded because label %s is not matching the expected value %s",
                                workflowNameLabel.get(), expectedWorkflowName));
                return Response.noContent().build();
            }

            if (jiraIssue.getFields().getStatus() == null) {
                logger.log(Level.INFO, String.format("Discarded because of missing field: %s.%s", FIELDS, STATUS));
                return Response.noContent().build();
            }
            JiraIssue.Status status = jiraIssue.getFields().getStatus();

            if (status.getStatusCategory() == null) {
                logger.log(Level.INFO,
                        String.format("Discarded because of missing field: %s.%s.%s", FIELDS, STATUS, STATUS_CATEGORY));
                return Response.noContent().build();
            }
            StatusCategory statusCategory = status.getStatusCategory();

            if (statusCategory.getKey() == null) {
                logger.log(Level.INFO, String.format("Discarded because of missing field: %s.%s.%s.%s", FIELDS, STATUS,
                        STATUS_CATEGORY, KEY));
                return Response.noContent().build();
            }
            String statusCategoryKey = statusCategory.getKey();

            logger.log(Level.INFO,
                    String.format("Received Jira issue %s with workflowId %s, workflowName %s and status %s", issueKey,
                            workflowId, workflowName, statusCategoryKey));
            if (!statusCategoryKey.equals("done")) {
                logger.log(Level.INFO, "Discarded because not a completed issue but " + statusCategoryKey);
                return Response.noContent().build();
            }

            ClosedJiraTicket ticket = ClosedJiraTicket.builder().ticketId(issueKey).workFlowInstanceId(workflowId)
                    .workflowName(workflowName).status(statusCategoryKey).build();
            logger.log(Level.INFO, "Created ticket " + ticket);
            CloudEvent newCloudEvent = CloudEventBuilder.v1()
                    .withDataContentType(MediaType.APPLICATION_JSON)
                    .withExtension("workflowid", workflowId)
                    .withExtension("workflowname", workflowName)
                    .withExtension("jiraissuekey", issueKey)
                    .withExtension("jiraissuestatus", statusCategoryKey)
                    .withId(UUID.randomUUID().toString())
                    .withType(cloudeventType)
                    .withSource(URI.create(cloudeventSource))
                    .withData(PojoCloudEventData.wrap(ticket,
                            mapper::writeValueAsBytes))
                    .build();

            logger.log(Level.INFO, "Emitting " + newCloudEvent);
            eventNotifier.emit(newCloudEvent);
            return Response.ok(ticket).build();
        } else {
            logger.log(Level.INFO, "Discarded because of missing field: key");
            return Response.noContent().build();
        }
    }
}