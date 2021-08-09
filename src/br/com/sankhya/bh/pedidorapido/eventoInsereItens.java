package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.util.Collection;

public class eventoInsereItens implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if(persistenceEvent.getModifingFields().isModifing("STATUS")){
            DynamicVO voOld = (DynamicVO) persistenceEvent.getOldVO();
            if("PG".equals(voOld.asString("STATUS"))){
                if (voOld.asBigDecimalOrZero("NUNOTA").compareTo(BigDecimal.ZERO)>0){
                    ErroUtils.disparaErro("Status não pode ser alterado, Pedido de Venda já foi gerado!");
                }
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        JapeWrapper proDAO = JapeFactory.dao("Produto");
        JapeWrapper iteDAO = JapeFactory.dao("AD_TBHITE");
        JapeWrapper papDAO = JapeFactory.dao("RelacionamentoParceiroProduto");//TGFPAP

        Collection<DynamicVO> pro = proDAO.find("AD_PEDRAPIDO = 'S'");
        for (DynamicVO proVO : pro){

            DynamicVO papVO = papDAO.findOne("CODPROD = ? AND CODPARC = ?"
                    ,proVO.asBigDecimal("CODPROD")
                    ,vo.asBigDecimal("CODPARC"));
            String prodRel = "N";
            if(papVO!=null){
                prodRel = "S";
            }

            iteDAO.create()
                    .set("NUNOTAPR",vo.asBigDecimal("NUNOTAPR"))
                    .set("CODPROD",proVO.asBigDecimal("CODPROD"))
                    .set("CODVOL","KG")
                    .set("PRODREL",prodRel)
                    .save();
        }
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
