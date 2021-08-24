package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class acaoFinalizaOC implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        for (Registro linha : linhas){
            if("PG".equals(linha.getCampo("STATUS"))) {
                String statusLog = contextoAcao.getParam("STATUSLOG").toString();
                linha.setCampo("STATUSLOG", statusLog);
            }
        }
        contextoAcao.setMensagemRetorno("Status Logistico alterado.");
    }
}
