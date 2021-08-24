package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class acaoLimpaIOC implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        for (Registro linha : linhas){
            if("PG".equals(linha.getCampo("STATUS"))) {
                ErroUtils.disparaErro("Não é permitido limpar OC para Status 'Pedido Gerado'");
            }
            linha.setCampo("ORDEMCARGA", null);
        }
    }
}
