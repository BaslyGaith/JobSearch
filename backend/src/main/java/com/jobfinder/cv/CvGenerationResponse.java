package com.jobfinder.cv;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Both language versions of one CV, plus what the posting was judged to test. */
@Data
@Builder
public class CvGenerationResponse {

    private PostingAnalysis analysis;
    private List<CvDocumentDto> documents;
}
