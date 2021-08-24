package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class eventoValidaItem implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        JapeWrapper papDAO = JapeFactory.dao("RelacionamentoParceiroProduto");//TGFPAP
        JapeWrapper cabDAO = JapeFactory.dao("AD_TBHCAB");//Cabeçalho Pedido Rapido
        boolean valida = false;
        valida = persistenceEvent.getModifingFields().isModifingAny("QTDNEG");
        if (valida){
            DynamicVO iteVO = (DynamicVO) persistenceEvent.getVo();
            DynamicVO cabVO = cabDAO.findOne("NUNOTAPR = ?",iteVO.asBigDecimal("NUNOTAPR"));

            if(!"P".equals(cabVO.asString("STATUS"))){
                ErroUtils.disparaErro("Modificação não permitida! Pedido não está mais pendente!");
            }

            DynamicVO papVO = papDAO.findOne("CODPROD = ? AND CODPARC = ?"
                    ,iteVO.asBigDecimal("CODPROD")
                    ,cabVO.asBigDecimal("CODPARC"));

            if(papVO==null && !"S".equals(iteVO.asString("Produto.USOPROD"))){
                ErroUtils.disparaErro("Modificação não permitida! Esse produto não esta relacionado a esse Parceiro!");
            }
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
