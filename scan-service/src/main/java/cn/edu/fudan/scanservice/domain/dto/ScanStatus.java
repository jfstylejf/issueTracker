package cn.edu.fudan.scanservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanStatus {

    private String status;
    private String toolName;
    private int scanTime;
    private String startScanTime;
    private String endScanTime;
    private String elapsedTime;


    public void updateElapsedTime(){
        int day = scanTime / (3600 * 24);
        int dayMod = scanTime % (3600 * 24);

        int hours = dayMod / 3600;
        int hoursMod = dayMod % 3600;

        int minutes = hoursMod / 60;
        int minutesMod = hoursMod % 60;

        StringBuilder stringBuilder = new StringBuilder ();
        if(day != 0){
            if(day == 1){
                stringBuilder.append (day + " day ");
            }else{
                stringBuilder.append (day + " days ");
            }
        }
        if(hours != 0){
            if(hours == 1){
                stringBuilder.append (hours + " hour ");
            }else{
                stringBuilder.append (hours + " hours ");
            }
        }
        if(minutes != 0){
            if(minutes == 1){
                stringBuilder.append (minutes + " minute ");
            }else{
                stringBuilder.append (minutes + " minutes ");
            }
        }
        if(minutesMod != 0){
            if(minutesMod == 1){
                stringBuilder.append (minutesMod + " second ");
            }else{
                stringBuilder.append (minutesMod + " seconds ");
            }
        }

        String result = stringBuilder.toString ();
        if(result.isEmpty ()){
            result = "0 second" ;
        }

        elapsedTime = result;

    }

    @Override
    public int hashCode() {
        return Objects.hash(toolName);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScanStatus scanStatus = (ScanStatus) o;
        return toolName.equals(scanStatus.getToolName ());
    }
}
