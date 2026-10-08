package com.example.lab1.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    /** Идентификатор исходного сообщения. */
    private String uid;
    /** Идентификатор операции. */
    private String operationUid;
    /** Время формирования ответа. */
    private String systemTime;
    /** Результат обработки запроса. */
    private Codes code;
    /** Код ошибки; пустая строка при успехе. */
    private ErrorCodes errorCode;
    /** Описание ошибки; пустая строка при успехе. */
    private ErrorMessages errorMessage;
    /** Годовая премия; null, если расчет не запрошен. */
    private Double annualBonus;
    /** Квартальная премия управленца; иначе null. */
    private Double quarterlyBonus;
}
