package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.controllers.ControllerExceptionHandler;
import gov.cabinetoffice.gap.adminbackend.controllers.FeedbackController;
import gov.cabinetoffice.gap.adminbackend.repositories.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@WebMvcTest(FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = { FeedbackController.class, ControllerExceptionHandler.class, LambdasInterceptor.class })
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Spy
    private FeedbackService feedbackService = new FeedbackService(feedbackRepository);

    // @WithAdminSession ?
    @Test
    public void addFeedbackWithSatisfactionScoreOnly() {
        // FeedbackEntity feedbackEntity =
        // FeedbackEntity.builder().satisfaction(1).build();
        // when(this.feedbackService.addFeedback(1, "")).thenReturn(feedbackEntity);
        //
        // feedbackService.addFeedback(1, null);
        feedbackService.addFeedback(1, null);
    }

    @Test
    public void addFeedbackWithSatisfactionScoreOutwithLimit() {
        feedbackService.addFeedback(9, null);
    }

    @Test
    public void addFeedbackWithCommentOnly() {
        feedbackService.addFeedback(0, "I'm satisfied!");
    }

    @Test
    public void addFeedbackWithNoScoreAndEmptyComment() {
        feedbackService.addFeedback(0, "");
    }

    @Test
    public void addFeedbackWithSatisfactionScoreAndComment() {
        feedbackService.addFeedback(0, "I'm so satisfied!");
    }

    @Test
    public void addFeedbackWithNoContent() {
        feedbackService.addFeedback(0, null);
    }

    @Test
    public void addFeedbackWithCommentExceedingLimit() {
        feedbackService.addFeedback(0,
                "NchDTIzIw1d7edg5uhL0GETy0bxCH0B6 VDp7RwZt4QUcfZfscvTCgMn9QqbUAm1k Lc6HuCC5uVekupUgpCospid7Ahdje65t F24ZtWz2jybEkN4VhPlnxREVNQT3HevO jJXwwBJaOVUgzsaotYqqZmCVIvlF0oYs oogxsdpsmaakHBVCFGl1oHip57jXfEdT D3LdlTtMzQ3eKwWJ0Q7ESoO9H8DuybV3 7xU7zV8sAODviCxvmrosXRVCUTJ7WhhV 1jFiISnw1hdm4R7gu0tcbfTdlRWCHppI ByUC37kb9myAP1YNwmyORHet8CMBdET9 kJ2Y6brEDscuuic0MIYNaIeoQvuXgQkz m1wrcaBXUyPo8M4giLTGfkjvqnpJlnXc CeIiGpVfkFp8feEaJcVUtlhB3TnIcxmR uup2wa7VLJM2FITa8BtlyMxB84y7WgHW wSXVfKTfxznIhqMgWdiNTsW3gRoeR6bE rFt0q9Gj8JllxW0j7ahbiBUG2tfDu2ta rzHc0DDnP2Xl85U0RQ0KCFOgYs6ztDz6 21DhyA52qz0aBsjqLtjTNlvGoq475i11 2FhmbBv1IWCWC5NyluQcLFFTY0tqhfGG iuW7svzXQPVFW2lDTVeuL5bmV1Dhxv27 P6IhuroVPpKAD5c3A5HTDsjVt5qP3VBF MApAgG9hNXCxoWYDIEVEmX1axMYffHhA mR4C9cBmvKA8SL2PjQ2lnjedbKc0V5uA kkqUR72an69Wb8oAHB8RwBpdpsgVyFhE BlMCn3fJMjQD5C2YNlEg9lq6XPRlYME4 mi0sVtsPKjF9JKDyIATADi0NuyYlPWb1 M9ci2rygTPMi9YW3y4c8dhHsFYSAT5Zo qHiRZBGlOvccNToIvqdaTIuL1vBsQhU8 UsGfCuxUaTrCd8jSa8vbGEO1j04FndN2 JAQBraiJjuQfE6AlEUBLm5jP7OJNItan fUbEJAVwl0VzwkSFdfJr3FIwr7YdmFcv WFCNtYNFND4hq7PRYHmFs1OW5a1RYCpp kpGxypeIepRNtMlaKtWeJzHCrwV303ip 6CVDZlteugWL4Mf2f3v7333Nndk6Ahzs nudb6R07KgsCpjpXKJv2sBOqG1SdpT4Z xl8BHl7L1DfKp9Dn8654vRr5eDWZv8j2 isPDTuHBQu0szzHJ1LuWLwwOyEnlynm7 jlvkXdyc60dXdj0scQiz8UF13ZEkIZaW W4qnWlwavdymTd3VFSV9aMv2kzre0E5f lFpbt2I462OL09RNdv7tqCCEVB3ygqDN pFHLSrcdQT34bQShBC6wK3xjhRRzSZEh Mer82GNIYWjydFQCsgMB32RwzL7nlarm ApZ7H5yLXqreEjqCxZ4W366qvX57tOIx cd4odg3X07cAq51fPVaAYWwKtQRoXGZD U1H7B1fEXeZ2prNDh01m4WDcTjjuDunV ducbv4NVNyY4LsnMvzpi3qkezL0Dxcuz t40WuX4fOcMmir8o3EvxK7bARM5Bnur7 b7U5HPxtXB2HXXloO7VehhBpYHmtxQ7O gNq1een0nYoSqGy4QEOrQlFY1VQzlL1I 7d0JQ6ShmFJadY06lUmMr81Kn3ZWJGmp Xcg7xUEXzmBWfc71Z56LbNrj7GCcIUI2 I5iYq9vdt3ikknHBRCkQouRpbfSJcp9z 8MPvGjdgCbAQ87kd2EinqqUB7ip3U6wq GH2r9fF1MvavHoEmgvF9GRCB6B3bz7iW NevDeQPFGGgMsVwooXbfPe7iAaF57cUe taJ23jK9zt5sUGcqHshMSy2jy1UimBe5 jHRmvyrApNM1rFZQtgth7TEzxIEBfRjk ckW7Ix22gT521Ug9cu7xstiao0TSS2pW HF8BG2jyueASEIOApTrRdW9uyfnmN3hf Gf8XFf21N9NlMO0U5f6X75REgwwG2k77 C9vbxy0ONtQCzhBFTevz6qV5i3La9fUM 7f49UoQuAUPKtT56cmmPXxReybHsL7NA SlmpU4TiMvg8wxigd7tzcsho53rZRxXt syKCPE6IVaT2chuUKYWQu26ODt7WtMgp");
    }

}
