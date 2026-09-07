package com.jobfinder.cv;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Renders a {@link CvDraft} as a .docx laid out to the CV brief: dark navy
 * header band, small-caps section titles on a tinted ground, accent colour per
 * job family, Calibri throughout.
 */
@Component
public class CvDocxExporter {

    private static final String NAVY = "1B2A4A";
    private static final String GREY = "6B7280";
    private static final String TINT = "F1F5F9";
    private static final String BODY = "1F2937";
    private static final int BODY_SIZE = 10;   // ~9.5pt rounds to 10 half-points pairs below
    private static final BigInteger MARGIN = BigInteger.valueOf(794); // ~1.4cm in twips

    public byte[] export(CvDocumentDto document, CvProfile profile) throws IOException {
        CvDraft draft = document.getDraft();
        String accent = stripHash(document.getAccentColor());
        boolean french = "fr".equalsIgnoreCase(document.getLanguage());

        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            setMargins(doc);

            headerBand(doc, profile, draft, accent);
            contactLine(doc, profile);

            sectionTitle(doc, french ? "Profil" : "Profile", accent);
            justified(doc, draft.getProfile());

            sectionTitle(doc, french ? "Expérience professionnelle" : "Professional experience", accent);
            for (CvDraft.ExperienceBlock block : draft.getExperiences()) {
                experienceHeading(doc, block, accent);
                bullets(doc, block.getBullets(), accent);
            }

            if (draft.getWhatIBring() != null && !draft.getWhatIBring().isEmpty()) {
                sectionTitle(doc, french ? "Ce que j'apporte à ce poste" : "What I bring to this role", accent);
                for (CvDraft.ValueBlock value : draft.getWhatIBring()) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setSpacingAfter(60);
                    XWPFRun heading = p.createRun();
                    heading.setText(value.getHeading());
                    heading.setBold(true);
                    heading.setColor(accent);
                    style(heading, BODY_SIZE);
                    heading.addBreak();
                    XWPFRun body = p.createRun();
                    body.setText(value.getBody());
                    body.setColor(BODY);
                    style(body, BODY_SIZE);
                }
            }

            sectionTitle(doc, french ? "Compétences clés" : "Core skills", accent);
            for (CvDraft.SkillLine skill : draft.getCoreSkills()) {
                XWPFParagraph p = doc.createParagraph();
                p.setSpacingAfter(40);
                XWPFRun label = p.createRun();
                label.setText(skill.getLabel() + "  ");
                label.setBold(true);
                label.setColor(NAVY);
                style(label, BODY_SIZE);
                XWPFRun values = p.createRun();
                values.setText(skill.getValues());
                values.setColor(BODY);
                style(values, BODY_SIZE);
            }

            if (draft.getSelectedProject() != null) {
                sectionTitle(doc, french ? "Projet sélectionné" : "Selected project", accent);
                CvDraft.ProjectBlock project = draft.getSelectedProject();
                XWPFParagraph p = doc.createParagraph();
                XWPFRun name = p.createRun();
                name.setText(project.getName());
                name.setBold(true);
                name.setColor(accent);
                style(name, BODY_SIZE);
                XWPFRun meta = p.createRun();
                meta.setText("   " + nullToEmpty(project.getOrganisation()) + "  ·  " + nullToEmpty(project.getDates()));
                meta.setColor(GREY);
                style(meta, BODY_SIZE - 1);
                bullets(doc, project.getBullets(), accent);
            }

            if (draft.getCertifications() != null && !draft.getCertifications().isEmpty()) {
                sectionTitle(doc, french ? "Certifications" : "Certifications", accent);
                bullets(doc, draft.getCertifications(), accent);
            }

            sectionTitle(doc, french ? "Formation" : "Education", accent);
            for (CvDraft.EducationLine line : draft.getEducation()) {
                XWPFParagraph p = doc.createParagraph();
                p.setSpacingAfter(40);
                XWPFRun qualification = p.createRun();
                qualification.setText(line.getQualification());
                qualification.setBold(true);
                qualification.setColor(BODY);
                style(qualification, BODY_SIZE);
                XWPFRun rest = p.createRun();
                rest.setText("   " + line.getInstitution() + "  ·  " + line.getDates());
                rest.setColor(GREY);
                style(rest, BODY_SIZE - 1);
            }

            if (draft.getFooter() != null) {
                XWPFParagraph footer = doc.createParagraph();
                footer.setSpacingBefore(200);
                XWPFRun run = footer.createRun();
                run.setText(draft.getFooter());
                run.setColor(GREY);
                run.setItalic(true);
                style(run, BODY_SIZE - 1);
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    /** Name in caps with letter spacing on a navy band, tagline in the accent colour. */
    private void headerBand(XWPFDocument doc, CvProfile profile, CvDraft draft, String accent) {
        XWPFParagraph band = doc.createParagraph();
        shade(band, NAVY);
        band.setSpacingBefore(0);
        band.setSpacingAfter(0);

        XWPFRun name = band.createRun();
        name.setText(profile.getFullName().toUpperCase());
        name.setBold(true);
        name.setColor("FFFFFF");
        name.setFontFamily("Calibri");
        name.setFontSize(20);
        setLetterSpacing(name, 40);
        name.addBreak();

        XWPFRun tagline = band.createRun();
        tagline.setText(nullToEmpty(draft.getTagline()));
        tagline.setColor(lighten(accent));
        tagline.setFontFamily("Calibri");
        tagline.setFontSize(11);
    }

    private void contactLine(XWPFDocument doc, CvProfile profile) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(80);
        p.setSpacingAfter(120);
        XWPFRun run = p.createRun();
        run.setText(String.join("  ·  ",
                nullToEmpty(profile.getEmail()),
                nullToEmpty(profile.getPhone()),
                nullToEmpty(profile.getLinkedinUrl()),
                nullToEmpty(profile.getLocation())));
        run.setColor(GREY);
        style(run, BODY_SIZE - 1);
    }

    private void sectionTitle(XWPFDocument doc, String title, String accent) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        p.setSpacingAfter(80);
        shade(p, TINT);
        leftRule(p, accent);
        XWPFRun run = p.createRun();
        run.setText(title.toUpperCase());
        run.setBold(true);
        run.setColor(NAVY);
        run.setFontFamily("Calibri");
        run.setFontSize(9);
        setLetterSpacing(run, 30);
    }

    /** Job title bold left, dates right-aligned on the same line, company below in accent. */
    private void experienceHeading(XWPFDocument doc, CvDraft.ExperienceBlock block, String accent) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(120);
        p.setSpacingAfter(0);
        setRightTabStop(p);

        XWPFRun title = p.createRun();
        title.setText(block.getTitle());
        title.setBold(true);
        title.setColor(BODY);
        style(title, BODY_SIZE);

        XWPFRun dates = p.createRun();
        dates.addTab();
        dates.setText(nullToEmpty(block.getDates()));
        dates.setColor(GREY);
        style(dates, BODY_SIZE - 1);

        XWPFParagraph company = doc.createParagraph();
        company.setSpacingAfter(60);
        XWPFRun companyRun = company.createRun();
        companyRun.setText(block.getCompany()
                + (block.getCompanyNote() == null ? "" : " (" + block.getCompanyNote() + ")")
                + (block.getLocation() == null ? "" : ", " + block.getLocation()));
        companyRun.setColor(accent);
        companyRun.setBold(true);
        style(companyRun, BODY_SIZE - 1);
    }

    private void bullets(XWPFDocument doc, List<String> items, String accent) {
        if (items == null) {
            return;
        }
        for (String item : items) {
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingAfter(40);
            p.setIndentationLeft(260);
            p.setIndentationHanging(180);
            XWPFRun marker = p.createRun();
            marker.setText("▪  ");
            marker.setColor(accent);
            style(marker, BODY_SIZE);
            XWPFRun text = p.createRun();
            text.setText(item);
            text.setColor(BODY);
            style(text, BODY_SIZE);
        }
    }

    private void justified(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);
        p.setSpacingAfter(80);
        XWPFRun run = p.createRun();
        run.setText(nullToEmpty(text));
        run.setColor(BODY);
        style(run, BODY_SIZE);
    }

    private void style(XWPFRun run, int size) {
        run.setFontFamily("Calibri");
        run.setFontSize(size);
    }

    private void setMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar margins = sectPr.addNewPgMar();
        margins.setLeft(MARGIN);
        margins.setRight(MARGIN);
        margins.setTop(MARGIN);
        margins.setBottom(MARGIN);
    }

    private void shade(XWPFParagraph paragraph, String hex) {
        CTShd shd = paragraph.getCTP().addNewPPr().addNewShd();
        shd.setVal(STShd.CLEAR);
        shd.setFill(hex);
    }

    private void leftRule(XWPFParagraph paragraph, String accent) {
        CTPBdr borders = paragraph.getCTP().getPPr().addNewPBdr();
        CTBorder left = borders.addNewLeft();
        left.setVal(STBorder.SINGLE);
        left.setSz(BigInteger.valueOf(18));
        left.setColor(accent);
        left.setSpace(BigInteger.valueOf(6));
    }

    private void setRightTabStop(XWPFParagraph paragraph) {
        CTTabs tabs = paragraph.getCTP().addNewPPr().addNewTabs();
        CTTabStop stop = tabs.addNewTab();
        stop.setVal(STTabJc.RIGHT);
        stop.setPos(BigInteger.valueOf(9600));
    }

    private void setLetterSpacing(XWPFRun run, int twentiethsOfPoint) {
        CTSignedTwipsMeasure spacing = run.getCTR().addNewRPr().addNewSpacing();
        spacing.setVal(BigInteger.valueOf(twentiethsOfPoint));
    }

    /** A readable tint of the accent for use on the dark band. */
    private String lighten(String hex) {
        try {
            int rgb = Integer.parseInt(hex, 16);
            int r = Math.min(255, ((rgb >> 16) & 0xFF) + 90);
            int g = Math.min(255, ((rgb >> 8) & 0xFF) + 90);
            int b = Math.min(255, (rgb & 0xFF) + 90);
            return String.format("%02X%02X%02X", r, g, b);
        } catch (NumberFormatException e) {
            return "93C5FD";
        }
    }

    private String stripHash(String color) {
        if (color == null || color.isBlank()) {
            return "1D4ED8";
        }
        return color.startsWith("#") ? color.substring(1).toUpperCase() : color.toUpperCase();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
