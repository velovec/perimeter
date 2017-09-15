package ru.v0rt3x.perimeter.server.web.views.traffic.tcp;

import org.apache.commons.lang.StringEscapeUtils;
import ru.v0rt3x.perimeter.server.web.views.service.Service;

import javax.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "traffic")
public class TCPPacket {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "service")
    private Service service;

    private Integer transmission;

    private String clientHost;
    private Integer clientPort;

    private boolean inbound;

    private String payload;

    private long time;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public byte[] getPayload() {
        return Base64.getDecoder().decode(payload);
    }

    public Integer getPayloadSize() {
        return getPayload().length;
    }

    public String getPayloadAsASCII() {
        byte[] data = getPayload();

        return StringEscapeUtils.escapeHtml(
            new String(data)
        );
    }

    public String getPayloadAsHEX() {
        byte[] data = getPayload();

        StringBuilder hexString = new StringBuilder();
        String asciiString = "";

        final int perRow = 16;

        int col = 0;
        int row = 0;

        for (int i = 0; i < data.length; i++) {
            if ((col >= perRow)) {
                hexString.append(" ").append(StringEscapeUtils.escapeHtml(asciiString));

                col = 0;
                row++;
            }

            if (col == 0) {
                hexString.append(String.format("\n%06X", row * perRow));
                asciiString = "";
            }

            hexString.append(String.format(" %02X", data[i]));
            asciiString += ((32 <= data[i])&&(data[i] <= 126)) ? String.format("%c", data[i]) : ".";

            if (i == data.length - 1) {
                int cols = perRow - col - 1;

                if (cols > 0) {
                    hexString.append(String.format(String.format("%%%ds", cols * 3), " "));
                    hexString.append(" ").append(StringEscapeUtils.escapeHtml(asciiString));
                }

                break;
            }

            col++;
        }

        return hexString.toString();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return String.format("TCPPacket<%s:%d> %s (%d bytes)", clientHost, clientPort, inbound ? "<=|" : "|=>", getPayloadSize());
    }

    public Integer getTransmission() {
        return transmission;
    }

    public void setTransmission(Integer transmission) {
        this.transmission = transmission;
    }

    public Integer getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
