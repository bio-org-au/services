<div id="productDescription" data-product="${params.product}"
     class="alert ${st.alertClass(product: params.product)} alert-dismissible text-default" role="alert">
  <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>
  </button>
  <st:productDescription product="${params.product}"/>
</div>

