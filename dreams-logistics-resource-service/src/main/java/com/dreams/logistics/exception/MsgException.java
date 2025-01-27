package com.dreams.logistics.exception;


import com.dreams.logistics.entity.FailMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MsgException extends RuntimeException {

    private FailMsg failMsg;

}
