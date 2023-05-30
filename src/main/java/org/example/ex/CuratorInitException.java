package org.example.ex;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CuratorInitException extends RuntimeException {

    public CuratorInitException(final Exception exception) {
        super(exception);
    }
}
