/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;
import io.realm.objectserver.ErrorCode;
/**
 * BINDING State. After bind() is called, this state will attempt to bind the local Realm to the remote. This is an
 * asynchronous operation, that must be able to be interrupted.
 */
public class BindingRealmState extends FsmState {

    @Override
    public void onEnterState() {
        if (session.isAuthenticated(session.configuration)) {
            // FIXME How to handle errors?
            session.bindWithTokens();
            gotoNextState(SessionState.BOUND);
        } else {
            // Not access token available. We need to authenticateUser first.
            gotoNextState(SessionState.AUTHENTICATING);
        }
    }

    @Override
    public void onExitState() {
        // TODO Abort any async stuff going on, possible in `session.bindWithTokens()`
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING_REALM); // Will trigger a retry.
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        session.replaceCredentials(credentials);
        gotoNextState(SessionState.BINDING_REALM); // Retry with new credentials
    }

    @Override
    public void onError(ErrorCode errorCode, String errorMessage) {
        // Ignore all errors. This is just a transient state. We are not bound yet, and any error should not
        // happen until we are BOUND.
    }
}
