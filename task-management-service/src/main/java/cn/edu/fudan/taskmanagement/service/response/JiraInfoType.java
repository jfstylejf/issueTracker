package cn.edu.fudan.taskmanagement.service.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class JiraInfoType {
    //单号
    private final String ticketNo;
    //单号对应的commit次数
    private final int quantity;
    private final String status;
    //是否有commit对应
    private final boolean linked;

    public JiraInfoType(String ticketNo, int quantity, String status, boolean linked) {
        this.ticketNo = ticketNo;
        this.quantity = quantity;
        this.status = status;
        this.linked = linked;
    }
}
