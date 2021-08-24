package br.com.sankhya.bh.pedidorapido;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;

import java.math.BigDecimal;
import java.util.Collection;

public class eventoInsereItens implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if(!persistenceEvent.getModifingFields().isModifingAny("NUNOTA,DTRETORNO,STATUSLOG")){
            DynamicVO voOld = (DynamicVO) persistenceEvent.getOldVO();
            if("PG".equals(voOld.asString("STATUS"))){
                if (voOld.asBigDecimalOrZero("NUNOTA").compareTo(BigDecimal.ZERO)>0){
                    ErroUtils.disparaErro("Pedido não pode ser alterado, Pedido de Venda já foi gerado!");
                }
            }

            if(persistenceEvent.getModifingFields().isModifingAny("DTRETORNO")){
                if(!"D".equals(voOld.asString("STATUSLOG"))){
                    ErroUtils.disparaErro("Dt. Retorno permitido penas para pedido devolvido!");
                }
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        if("PG".equals(vo.asString("STATUS"))){
            if (vo.asBigDecimalOrZero("NUNOTA").compareTo(BigDecimal.ZERO)>0){
                ErroUtils.disparaErro("Pedido não pode ser deletado, Pedido de Venda já foi gerado!");
            }
        }
    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        JapeWrapper proDAO = JapeFactory.dao("Produto");
        JapeWrapper iteDAO = JapeFactory.dao("AD_TBHITE");
        JapeWrapper tbhCabDAO = JapeFactory.dao("AD_TBHCAB");
        JapeWrapper papDAO = JapeFactory.dao("RelacionamentoParceiroProduto");//TGFPAP
        JapeWrapper parDAO = JapeFactory.dao("Parceiro");

        ImpostosHelpper impHelper = new ImpostosHelpper();

        Collection<DynamicVO> pro = proDAO.find("AD_PEDRAPIDO = 'S'");
        for (DynamicVO proVO : pro){

            DynamicVO papVO = papDAO.findOne("CODPROD = ? AND CODPARC = ?"
                    ,proVO.asBigDecimal("CODPROD")
                    ,vo.asBigDecimal("CODPARC"));
            String prodRel = "N";
            if(papVO!=null){
                prodRel = "S";
            }

            DynamicVO tbhCabVO = tbhCabDAO.findOne("NUNOTAPR = ?",vo.asBigDecimal("NUNOTAPR"));
            DynamicVO parVO = parDAO.findOne("CODPARC = ?", tbhCabVO.asBigDecimal("CODPARC"));

            BigDecimal precoTab = impHelper.buscaPrecoTabelaAtual(parVO.asBigDecimal("CODTAB")
                    , proVO.asBigDecimal("CODPROD"));

            iteDAO.create()
                    .set("NUNOTAPR",vo.asBigDecimal("NUNOTAPR"))
                    .set("CODPROD",proVO.asBigDecimal("CODPROD"))
                    .set("CODVOL","KG")
                    .set("PRODREL",prodRel)
                    .set("VLRUNIT",precoTab)
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
