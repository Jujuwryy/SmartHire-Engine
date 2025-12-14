package com.george.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.george.dto.JobMatchResponse;
import com.george.exception.ErrorCode;
import com.george.exception.ExportException;
import com.george.model.JobMatch;
import com.george.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private final ObjectMapper objectMapper;

    public ExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String exportToJson(JobMatchResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("JobMatchResponse cannot be null");
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Failed to export job matches to JSON", e);
            throw new ExportException(ErrorCode.EXPORT_JSON_ERROR, "Failed to export job matches to JSON", e);
        }
    }

    public String exportToCsv(JobMatchResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("JobMatchResponse cannot be null");
        }
        
        StringBuilder csv = new StringBuilder();
        
        csv.append("Job Title,Company,Location,Employment Type,Experience,Required Techs,Salary Min,Salary Max,Currency,Confidence,Match Reasons\n");
        
        List<JobMatch> matches = response.getMatches();
        if (matches != null) {
            for (JobMatch match : matches) {
                if (match == null || match.getJob() == null) {
                    continue;
                }
                
                Post job = match.getJob();
                csv.append(escapeCsvField(job.getJobTitle())).append(",");
                csv.append(escapeCsvField(job.getCompany())).append(",");
                csv.append(escapeCsvField(job.getLocation())).append(",");
                csv.append(escapeCsvField(job.getEmploymentType())).append(",");
                csv.append(job.getExperience() != null ? job.getExperience() : "").append(",");
                csv.append(escapeCsvField(formatList(job.getRequiredTechs()))).append(",");
                csv.append(job.getSalaryMin() != null ? job.getSalaryMin() : "").append(",");
                csv.append(job.getSalaryMax() != null ? job.getSalaryMax() : "").append(",");
                csv.append(escapeCsvField(job.getCurrency())).append(",");
                csv.append(String.format("%.2f", match.getConfidence())).append(",");
                csv.append(escapeCsvField(formatList(match.getMatchReasons()))).append("\n");
            }
        }
        
        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join("; ", list);
    }
}

