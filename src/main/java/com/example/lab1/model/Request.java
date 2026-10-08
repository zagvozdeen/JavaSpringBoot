package com.example.lab1.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
public class Request {
    /** Уникальный идентификатор сообщения. */
    @NotBlank
    @Size(max = 32)
    private String uid;

    /** Уникальный идентификатор операции. */
    @NotBlank
    @Size(max = 32)
    private String operationUid;

    /** Система-отправитель. */
    private Systems systemName;

    /** Время формирования сообщения. */
    @NotBlank
    private String systemTime;

    /** Источник сообщения. */
    private String source;

    /** Идентификатор коммуникации. */
    @Min(1)
    @Max(100000)
    private int communicationId;

    /** Идентификатор шаблона. */
    private int templateId;
    /** Код продукта. */
    private int productCode;
    /** Код SMS. */
    private int smsCode;

    /** Должность сотрудника. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Positions position;

    /** Заработная плата для расчета премии. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double salary;

    /** Множитель премии. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double bonus;

    /** Количество отработанных дней за год. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer workDays;
}
