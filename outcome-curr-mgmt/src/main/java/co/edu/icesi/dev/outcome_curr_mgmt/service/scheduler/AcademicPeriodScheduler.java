package co.edu.icesi.dev.outcome_curr_mgmt.service.scheduler;

import co.edu.icesi.dev.outcome_curr.mgmt.model.stdindto.management.AcadPeriodInDTO;
import co.edu.icesi.dev.outcome_curr_mgmt.service.management.AcPeriodService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AcademicPeriodScheduler {

    private final AcPeriodService acPeriodService;

    public AcademicPeriodScheduler(AcPeriodService acPeriodService) {
        this.acPeriodService = acPeriodService;
    }

    // Ejecutar la consulta periódica cada 30 segundos
    @Scheduled(fixedRate = 30000)
    public void performPeriodicQueries() {
        acPeriodService.getAllAcademicPeriods();
    }

    // Ejecutar pruebas periódicas sobre el servicio de creación cada 1 minuto
    @Scheduled(fixedRate = 60000)
    public void performPeriodicCreation() {
        String acPeriodNameEng = "Academic Period " + System.currentTimeMillis();
        String setAcPeriodNameSpa = "Periodo Academico " + System.currentTimeMillis();
        int setAcPeriodNumeric = (int) (Math.random() * 10000);

        AcadPeriodInDTO newAcademicPeriod = new AcadPeriodInDTO(acPeriodNameEng, setAcPeriodNameSpa,
                setAcPeriodNumeric);

        acPeriodService.addAcademicPeriod(newAcademicPeriod);
    }
}
