package br.com.thiengo.androidblogapp.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import br.com.thiengo.androidblogapp.presenter.PresenterLogin;
import br.com.thiengo.androidblogapp.presenter.User;

public class LoginActivity extends AppCompatActivity {
    /*
     * CÓDIGO INTEIRO ALEATÓRIO PARA POSTERIOR VERIFICAÇÃO
     * EM onActivityResult()
     * */
    public static final int APP_REQUEST_CODE = 665;

    private PresenterLogin presenter;
    public static boolean isOpened;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * PARA POSTERIOR ENVIO / VERIFICAÇÃO DE DADOS
         * NO BACKEND WEB
         * */
        presenter = PresenterLogin.getInstance( LoginActivity.this );

        /* VERIFICA SE O USUÁRIO JÁ ESTÁ CONECTADO */
        if (AccountKit.getCurrentAccessToken() != null) {
            getUserLoginData();
        }
        else{
            onLoginEmail();
        }

        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancelAll();
    }

    public void onLoginEmail() {
        /*
         * DEFINIÇÃO COMPLETA PARA QUE SEJA APRESENTADA
         * UMA ACTIVITY DE LOGIN COM SOLICITAÇÃO DE EMAIL
         * */
        Intent intent = new Intent(this, AccountKitActivity.class);

        AccountKitConfiguration
            .AccountKitConfigurationBuilder configurationBuilder =
            new AccountKitConfiguration
                    .AccountKitConfigurationBuilder(
                        LoginType.EMAIL,
                        AccountKitActivity.ResponseType.TOKEN );

        intent.putExtra(
            AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
            configurationBuilder.build() );

        startActivityForResult( intent, APP_REQUEST_CODE );
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {

            /* ACESSANDO O RESULTADO DA ACTIVITY DE LOGIN */
            AccountKitLoginResult loginResult = data.getParcelableExtra(
                    AccountKitLoginResult.RESULT_KEY );

            if (loginResult.getError() != null) {
                String mensagem = loginResult.getError().getErrorType().getMessage();
                Toast.makeText( this, mensagem, Toast.LENGTH_LONG ).show();
            }
            else {
                /*
                 * TUDO CERTO, VAMOS A OBTENÇÃO DE DADOS DE LOGIN (EMAIL E ID)
                 * E ASSIM PROSSEGUIR COM O ACESSO A ÁREA DE POSTS
                 * */
                getUserLoginData();
            }
        }
    }

    private void getUserLoginData(){
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                User u = new User( account.getId(), account.getEmail() );
                presenter.verifyLogin( u );
            }

            @Override
            public void onError(final AccountKitError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* PARA EVITAR VAZAMENTO DE MEMÓRIA */
        PresenterLogin.clearInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        isOpened = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOpened = false;
    }
}

