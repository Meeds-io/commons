<%--

    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="org.exoplatform.container.PortalContainer" %>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService" %>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.resource.SkinService"%>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.gatein.common.text.EntityEncoder" %>
<%@ page language="java" %>
<%
    String contextPath = request.getContextPath();
    String lang = request.getLocale().getLanguage();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

    PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
    UserPortalConfigService userPortalConfigService = portalContainer.getComponentInstanceOfType(UserPortalConfigService.class);
    String skinName = userPortalConfigService.getDefaultPortalSkinName();
    SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);
    String cssPath = skinService.getSkin("portal/Conditions", skinName).getCSSPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=lang%>" lang="<%=lang%>">
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
		<title>eXo Subscription Agreement</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

		<link href="<%=cssPath%>" rel="stylesheet" type="text/css"/>
	</head>
	<body>
		<div class="backLight"></div>
		<div class="uiWelcomeBox" id="AccountSetup">
			<div class="header">Terms and Conditions Agreement</div>
			<div class="content" id="AccountSetup">
                    <p class="c68 c86"><span class="c2">Last updated: October 2nd, 2017</span></p>
                    <p class="c86 c68"><span class="c3">eXo Platform SAS</span></p>
                    <p class="c68 c36 c97"><span class="c25"></span></p>
                    <h1 class="c112" id="h.gjdgxs"><span class="c3">Master Subscription Agreement</span></h1>
                    <p class="c73 c68"><span class="c10">PLEASE READ THIS MASTER SUBSCRIPTION AGREEMENT BEFORE PURCHASING OR USING THE PRODUCTS OR SERVICES. BY USING OR PURCHASING THE PRODUCTS OR SERVICES, CUSTOMER SIGNIFIES ITS ASSENT TO THIS AGREEMENT. IF YOU ARE ACTING ON BEHALF OF AN ENTITY, YOU REPRESENT THAT YOU HAVE THE AUTHORITY TO ENTER INTO THIS AGREEMENT ON BEHALF OF THAT ENTITY. IF CUSTOMER DOES NOT ACCEPT THE TERMS OF THIS AGREEMENT, THEN IT MUST NOT PURCHASE NOR USE THE PRODUCTS OR SERVICES.</span></p>
                    <p class="c1 c36"><span class="c21"></span></p>
                    <p class="c1"><span class="c10">This </span><span class="c3">Master Subscription Agreement </span><span class="c10">(the </span><span class="c3">&ldquo;Agreement&rdquo;</span><span class="c10">) is entered into by and between eXo Platform (either &ldquo;eXo Platform SAS&rdquo; or &ldquo;eXo Platform NA LLC&rdquo; or &ldquo;eXo Platform Luxembourg SARL&rdquo;) as referred in the Order Form or any other order form related to the purchase of products and/or services related to this Agreement and, if there is no precision, with eXo Platform NA LLC, a limited liability company of the laws of California registered before the registry of Californian companies under number 200920410095 and having its principal office 51 Federal Street, Suite 350, San Francisco, California 94105, United-States (</span><span class="c3">&ldquo;eXo&rdquo;</span><span class="c10">) and the purchaser or user of eXo products and/or services that accepts the terms of this Agreement (</span><span class="c3">&ldquo;Customer&rdquo;</span><span class="c10">). The effective date of this Agreement (</span><span class="c3">&ldquo;Effective Date&rdquo;</span><span class="c10">) is the earlier of these three dates: the date of signature or acceptance of this Agreement by entering into an Order Form by the Customer, the date of use of eXo products and/or services by the Customer, or the date of purchase and payment of eXo products and/or services through eXo online selling platform.</span></p>
                    <p class="c1"><span class="c10">Whereas eXo and Customer desire to establish certain terms and conditions under which Customer will, from time to time, be licensed software and obtain services from eXo;</span></p>
                    <p class="c1"><span class="c10">Now, therefore, for good and valuable consideration, the receipt and sufficiency of which is hereby acknowledged, Customer and eXo agree as follows:</span></p>
                    <p class="c1"><span class="c3">1. Definitions</span></p>
                    <p class="c1"><span class="c10">Capitalized terms used in this Agreement are defined in this Section 1 or the Section in which they are first used:</span></p>
                    <p class="c1"><span class="c3">1.1 &ldquo;Activation Key&rdquo;</span><span class="c10">&nbsp;means a file evidencing a grant of one or more Licenses by eXo to Customer for the Term, and provided to Customer when the Subscription is purchased.</span></p>
                    <p class="c1"><span class="c3">1.2 &ldquo;CORE Processor&rdquo;</span><span class="c10">&nbsp;means the virtual or real unit that reads and executes program instructions.</span></p>
                    <p class="c1"><span class="c3">1.3 &ldquo;Documentation&rdquo;</span><span class="c10">&nbsp;means the standard end-user technical documentation and specifications that eXo supplies with the Software, as revised from time to time by eXo. Advertising and marketing materials are not Documentation.</span></p>
                    <p class="c1"><span class="c3">1.4 &ldquo;Error&rdquo;</span><span class="c10">&nbsp;means a reproducible failure of the Software to perform in substantial conformity with its Documentation, and considered as such by eXo.</span></p>
                    <p class="c62"><span class="c3">1.5 &ldquo;eXo Add-Ons&rdquo;</span><span class="c10">&nbsp;means an extension module or plugin published by eXo enhancing the Software with additional functionalities.</span></p>
                    <p class="c1"><span class="c3">1.6 &ldquo;License&rdquo;</span><span class="c10">&nbsp;means a license granted, concurrently to a Subscription Plan, by eXo to Customer to allow installation and use of the Software.</span></p>
                    <p class="c62"><span class="c3">1.7 &ldquo;Third-Party Software&rdquo;</span><span class="c10">&nbsp;means various third-party software components licensed under the terms of applicable license agreements, whether open source or not, included in the materials relating to such software. Third-Party Software is composed of individual software components, each of which has its own copyright and its own applicable license conditions.</span></p>
                    <p class="c1"><span class="c3">1.8 &ldquo;Order Form&rdquo;</span><span class="c10">&nbsp;means an order form, whether in written or electronic form, composed of one or multiple purchase orders, defining the Software, and/or the services which will be delivered to the Customer by eXo, in accordance with this Agreement and the specific conditions written in this Order Form.</span></p>
                    <p class="c68 c103"><span class="c3">1.9 &ldquo;Software&rdquo;</span><span class="c10">&nbsp;means the tested and certified software in object code format provided by eXo to Customer, as specified on the Order Form, pursuant to this Agreement as of the Effective Date or a future date, including any Documentation incorporated therein, and Updates to such software that eXo may provide to Customer from time to time as part of eXo Products Maintenance Program, as defined at <a href="https://www.exoplatform.com/maintenance-program">https://www.exoplatform.com/maintenance-program</a>. For the avoidance of doubt and unless otherwise specified, Third-Party Software and/or eXo Add-Ons, that may be available through the Software, (a) do not form a part of the Software, (b) are solely governed by their own licenses and/or terms and conditions, (c) are not covered by this Agreement, and (d) may be downloaded and installed by the Customer under its sole responsibility and liability. As a limited exception to the aforesaid, eXo Add-Ons listed in </span><span class="c3">Appendix 4 </span><span class="c10">(&ldquo;</span><span class="c3">eXo</span><span class="c10">&nbsp;</span><span class="c3">Official Add-Ons</span><span class="c10">&rdquo;) are governed and supported under the conditions of this Agreement under the same level of Support Services as the Support Services applicable to the Software. </span></p>
                    <p class="c1"><span class="c3">1.10 &ldquo;Subscription&rdquo;</span><span class="c10">&nbsp;or </span><span class="c3">&ldquo;Subscription Plan&rdquo; </span><span class="c10">mean equally eXo&rsquo;s commercial offerings that include the benefits and limitations listed in Appendix 0, the License for the Software and, if applicable, the access to Support Services and/or access to the Hosting and Managed Services ordered and paid for by Customer and provided by eXo as specified on the Order Form.</span></p>
                    <p class="c1"><span class="c3">1.11 &ldquo;Support Services&rdquo;</span><span class="c10">&nbsp;means the support services provided by eXo to Customer as part of a Subscription Plan and as further defined in Section 2.6 and <a href="https://www.exoplatform.com/legal/sla.pdf">https://www.exoplatform.com/legal/sla.pdf</a>&nbsp;and/or as may be specified on the applicable Order Form.</span></p>
                    <p class="c1"><span class="c3">1.12 &ldquo;Term&rdquo;</span><span class="c10">&nbsp;means the period of time for the Subscription as specified in the Order Form.</span></p>
                    <p class="c1"><span class="c3">1.13 &ldquo;Update&rdquo;</span><span class="c10">&nbsp;means a Major Release, Minor Release or Maintenance Fix of the Software. </span><span class="c3 c12">&ldquo;Major Release&rdquo;</span><span class="c10">&nbsp;means a later version of the Software identified by a change in the first digit (X) of the identified update according to the (X.y.z) schema. </span><span class="c3 c12">&ldquo;Minor Release&rdquo;</span><span class="c10">&nbsp;means a later version of the Software identified by a change in the second digit (Y) of the identified update according to the (x.Y.z) schema. </span><span class="c3 c12">&ldquo;Maintenance Fix&rdquo;</span><span class="c10">&nbsp;means a new version of the Software identified by a change in the third digit (Z) of the identified update according to the (x.y.Z) schema.</span></p>
                    <p class="c1"><span class="c3">1.14 &ldquo;Registered User&rdquo;</span><span class="c10">&nbsp;means a physical or virtual person or program, named or anonymous, who establishes a network connection with the server on which the Software is installed, with the objective to make partial or complete use of the Software. </span></p>
                    <p class="c1"><span class="c3">2. General Terms</span></p>
                    <p class="c1"><span class="c3">2.1 Scope of Agreement. </span><span class="c10">This Agreement governs all transactions between the parties with respect to the Software and Services provided hereunder.</span></p>
                    <p class="c1"><span class="c3">2.2 Orders. </span><span class="c10">Customer may place orders with eXo to purchase a Subscription at eXo&rsquo;s then-current prices. Customer may transmit such orders to eXo by mail, fax, email or other electronic channels. Customer may, for its convenience, submit orders using its standard forms, but no terms, provisions or conditions of any order document, acknowledgement or other business form that Customer may use in connection with the acquisition or licensing of the Software will have any effect on the rights, duties or obligations of the parties under, or otherwise modify this Agreement, regardless of any failure of eXo to object to such terms, provisions or conditions. Any such additional or conflicting terms and conditions on any Customer order document, acknowledgement or other business form are hereby rejected by eXo.</span></p>
                    <p class="c1"><span class="c3">2.3. Acceptance of Orders.</span><span class="c10">&nbsp;eXo may accept orders in its sole discretion by sending to Customer an Order Form confirming the particulars of the order.</span></p>
                    <p class="c1"><span class="c3">2.4 Delivery </span><span class="c10">Delivery of the Software will be from eXo&rsquo;s Customer Portal web site. The Term is specified in the Order Form. The Software will be deemed accepted by Customer upon delivery of the Activation Key.</span></p>
                    <p class="c1"><span class="c3">2.5 Installation. </span><span class="c10">Customer will be responsible for installing the Software on its computers as permitted under this Agreement. Installation services may be purchased on at time-and-materials basis at eXo&rsquo;s then-current rates as specified on the applicable Order Form.</span></p>
                    <p class="c1"><span class="c3">2.6 Support Services.</span><span class="c2">&nbsp;During the time that Customer has paid the applicable annual Subscription fees, eXo will provide Customer with Support Services for the Software according to the purchased Subscription Plan, under the terms outlined in the Support Policy as defined in Appendix 1 of this Agreement. Such Support Services are provided to Customer solely for Customer&rsquo;s internal use, and Customer may not use the Support Services to supply any consulting, support or training services to any third party.</span></p>
                    <p class="c118 c68"><span class="c13">2.6Bis Hosted and Managed Services</span><span class="c69">. During the time that Customer has paid the applicable annual Subscription fees, eXo will provide Customer with Hosting and Managed Services for the Software according to the purchased Subscription Plan purchased, under the contractual and operational terms as defined in Appendix 3 &ldquo;Hosting and Managed Services&rdquo;. These Hosting and Managed Services are provided to Customer solely for Customer&rsquo;s internal use, and Customer may not use the Hosting and Managed Services to any third party.</span></p>
                    <p class="c1"><span class="c3">2.7 Exclusions.</span><span class="c10">&nbsp;eXo will have no obligation to correct Errors caused by: (a) improper installation of the Software; (b) altered or modified Software, unless altered or modified by eXo; (c) use of the Software in a manner inconsistent with its Documentation or this Agreement; (d) any combination of the Software with hardware or software not specified in the Documentation; or (e) defects in the Software due to accident, hardware malfunction, abuse or improper use.</span></p>
                    <p class="c1"><span class="c3">2.8 Additional Services. </span><span class="c10">Should Customer request that eXo provide services in connection with problems (a) caused by the factors listed in Section 2.7 or (b) that are otherwise beyond the scope of the Support Services or this Agreement, Customer will pay for such services eXo agrees to perform on a time-and-materials basis at then-current rates.</span></p>
                    <p class="c1"><span class="c3">2.9 Customer Obligations.</span></p>
                    <p class="c1"><span class="c3">2.9.1</span><span class="c10">&nbsp;As a condition to eXo&rsquo;s provision of the Support Services, Customer agrees to assure necessary competence for use of the Software. Training courses (Appendix 2) provide the Customer&rsquo;s technical crew with the set of knowledge required.</span></p>
                    <p class="c1"><span class="c3">2.9.2</span><span class="c10">&nbsp;As a condition to eXo&rsquo;s provision of the Support Services, Customer must assist eXo in identifying and correcting any Errors, including executing reasonable diagnostic routines in accordance with any instructions provided by eXo. Customer agrees to provide eXo with such cooperation, materials, information, access, and support which eXo deems to be reasonably required to allow eXo to successfully provide the Support Services, including, without limitation, as may be set forth in an applicable Order Form. Customer understands and agrees that eXo&rsquo;s obligations hereunder are expressly conditioned upon Customer providing such cooperation, materials, information, access and support.</span></p>
                    <p class="c1"><span class="c3">2.9.3 </span><span class="c10">Customer acknowledges that in order for eXo to provide the Support Services, Customer may be required to license and install certain third party software and provide certain third party hardware that are not provided or licensed by eXo (</span><span class="c3">&ldquo;Third Party Products&rdquo;</span><span class="c10">). eXo may provide Customer with links and instructions for obtaining Third Party Products, but it is Customer&rsquo;s responsibility to properly license and install any required Third Party Products from the relevant third party providers. eXo will have no liability with respect to any Third Party Products. In the event of a failure by Customer to timely provide Third Party Products as required, eXo may treat the applicable Order Form as having been cancelled by Customer.</span></p>
                    <p class="c1"><span class="c3">3. Licenses</span></p>
                    <p class="c1"><span class="c3">3.1 Grant.</span><span class="c10">&nbsp;For each Subscription Plan that Customer purchases, eXo grants Customer a limited, non-exclusive, non-transferable, non-sublicensable (except as provided in Section 11.3) License for the Term to:</span></p>
                    <p class="c1"><span class="c3">a)</span><span class="c10">&nbsp;use, install and execute the Software licensed hereunder (in object code format) on any computers solely for Customer&rsquo;s own business purposes;</span></p>
                    <p class="c1"><span class="c3">b)</span><span class="c10">&nbsp;use, install the Software licensed hereunder (in object code format) with respect for the number of allowed CORE Processors and/or the limitation of Registered Users, as designated in the applicable Order Form, solely for Customer&rsquo;s own business purposes; </span></p>
                    <p class="c1"><span class="c10">Each License is subject to the terms and conditions of this Agreement, including the restrictions set forth in this Section 3 and will be contingent upon Customer&rsquo;s timely payment of eXo&#39;s applicable Subscription fee (as specified on the Order Form) and issuance by eXo of the Activation Key. The License granted herein is solely to the entity specified as &ldquo;Customer&rdquo; and not, by implication or otherwise, to any parent, subsidiary or affiliate of such entity.</span></p>
                    <p class="c1"><span class="c3">3.2 Copies.</span><span class="c10">&nbsp;Customer may make up to two (2) copies of the Software licensed hereunder for archival, backup, installation or disaster recovery purposes only. Customer will include in any such copy all copyright, trademark, or other proprietary rights notices as included in or affixed to the original Software.</span></p>
                    <p class="c1"><span class="c3">3.3 Restrictions.</span><span class="c10">&nbsp;Customer shall not itself, or through any parent, subsidiary, affiliate, agent or other third party:</span></p>
                    <p class="c1"><span class="c10">a) decompile, disassemble, translate, reverse engineer or otherwise attempt to derive source code from the Software, in whole or in part, nor will Customer use any mechanical, electronic or other method to trace, decompile, disassemble, or identify the source code of the Software or encourage others to do so, except to the limited extent, if any, that applicable law permits such acts notwithstanding any contractual prohibitions, provided, however, before Customer exercises any rights that Customer believes to be entitled to based on mandatory law, Customer shall provide eXo with thirty (30) days prior written notice and provide all reasonably requested information to allow eXo to assess Customer&rsquo;s claim and, at eXo&rsquo;s sole discretion, to provide alternatives that reduce any adverse impact on eXo&rsquo;s intellectual property or other rights; </span></p>
                    <p class="c1"><span class="c10">(b) allow access or permit use of the Software by any users other than Customer&rsquo;s employees, or authorized third-party contractors who are providing services to Customer and agree in writing to abide by the terms of this Agreement, provided further that Customer shall be liable for any failure by such employees and third-party contractors to comply with the terms of this Agreement, </span></p>
                    <p class="c1"><span class="c10">(c) create, develop, license, install, use, or deploy any third party software or services to circumvent, enable, modify or provide access, permissions or rights which violate the technical restrictions of the Software, any additional licensing terms provided by eXo via product documentation, notification, and the terms of this Agreement, </span></p>
                    <p class="c1"><span class="c10">(d) modify or create derivative works based upon the Software, </span></p>
                    <p class="c1"><span class="c10">(e) use the Software in connection with any business operation for which Customer provides services to third parties, or </span></p>
                    <p class="c1"><span class="c10">(f) disclose the results of any benchmark test of the Software to any third party without eXo&rsquo;s prior written approval, unless otherwise expressly permitted herein, provided, however, that the foregoing restriction shall apply to Customer only if Customer is a software or hardware vendor, or if Customer is performing testing or benchmarking on the Software.</span></p>
                    <p class="c62"><span class="c3">3.4 Third-Party Software.</span><span class="c10">&nbsp;The Third-Party Software is licensed under the terms of the applicable license conditions, whether open source or not, and/or copyright notices that can be found in the licenses file, the documentation or other materials accompanying the Third-Party Software. Copyrights to the Third-Party Software are held by copyright holders indicated in the copyright notices in the corresponding source files or in the licenses file or other materials accompanying the Third-Party Software.</span></p>
                    <p class="c1"><span class="c3">4. License Fees and Payment</span></p>
                    <p class="c1"><span class="c3">4.1 Subscription Fees.</span><span class="c2">&nbsp;Customer shall pay all fees for each Subscription Plan as specified on the applicable Order Form. Customer may purchase additional Subscription Plans by placing any order to eXo &nbsp;in accordance with Section 2.2. Any added Subscription will be subject to the following: &nbsp;(i) added Licenses will be coterminous with the pre-existing Term (either the initial Term or the renewal Term); (ii) the fees for the added Subscriptions will be the then-current, generally applicable Subscription fee for such; and (iii) any Subscription added during the validity period will be billed to the remaining pro rata of that period. eXo reserves the right to modify its Subscription Fees at any time, upon at least thirty (30) days prior notice to Customer, which notice may be provided by e-mail.</span></p>
                    <p class="c68 c118"><span class="c13">4.1Bis Declaration of Use </span><span class="c69">During the validity period of a Subscription Plan, eXo may request at any time Customer to fill a form to declare effective use of the License(s) granted for the Software by this Agreement. The Customer will fulfill this formality within fifteen (15) calendar days. In the event that the Declaration of Use would demonstrate a surplus use of the Software from the benefits granted by the Subscription Plan initially ordered in an applicable Order Form or according to the terms of this agreement, eXo shall adjust automatically the Subscription plan of the Customer with an then-current eligible Subscription Plan, and shall invoice &nbsp;immediately and retroactively the amount</span><span class="c10">&nbsp;corresponding to the difference with the induced Subscription Fee</span><span class="c69">. Reference dates used to calculate this amount will be the latest Effective date and End date written in the applicable Order Form. </span></p>
                    <p class="c1"><span class="c3">4.2 Billing and Renewal.</span><span class="c10">&nbsp;eXo charges for and collects in advance the Subscription Fees. eXo will automatically renew and issue an invoice each billing period thirty (30) days before the subsequent anniversary of the Subscription unless either party gives written notice of its intent not to renew at least ninety (90) days prior to the end of the current contract term. Upon any renewal, eXo&rsquo;s then-current terms and conditions for this Agreement will apply, including Support Services terms, Hosting and Managed Services terms and all other terms forming the entire applicable Agreement. Any specific term, commercial discount, specific payment condition appearing in the Order Form corresponding to the previous Subscription Plan are not automatically renewed. The Subscription Fees corresponding to the renewal will be corresponding to the then-current list price of the current Subscription Plan or its equivalent to date of invoicing the renewal. For the avoidance of doubt, the equivalency is established according to, at least, the parameters (a) of Duration AND (b) the number of CORE Processors and/or Registered users, AND (c) the Support Services tier and/or Hosting and Managed Services initially ordered and paid by the Customer. The current list of Subscription Plans is maintained and published by eXo at <a href="https://www.exoplatform.com/terms-conditions/subscription-plans.pdf">https://www.exoplatform.com/terms-conditions/subscription-plans.pdf</a>&nbsp;and in Appendix 0 of this Agreement. eXo&rsquo;s then-current list prices shall increase by up to 7 % above the applicable list prices in the prior term. eXo will provide written notice of updated pricing at least thirty (30) days prior to the applicable renewal term. &nbsp;Fees for any other services will be charged on an as-quoted basis. All eXo supplied Software and Support Services will only be delivered to Customer electronically through the Internet. Unless otherwise specified on an Order Form, all invoices will be paid within thirty (30) days from the date of the invoice. Subscription Fees are non-refundable after payment. Payments will be made without right of set-off or chargeback. All payments must be made in the currency stated in the Order Form. Late payments will accrue interest at the rate of one and one half percent (1&frac12;%) per month, or, if lower, the maximum rate permitted under applicable law. If payment of any fee is overdue, eXo may also suspend provision of the Services and the License of the Software until such delinquency is corrected. </span></p>
                    <p class="c1"><span class="c3">4.3 Taxes.</span><span class="c10">&nbsp;The amounts payable to eXo under this Agreement do not include any taxes, levies, or similar governmental charges, however designated, including any related penalties and interest (</span><span class="c3">&ldquo;Taxes&rdquo;</span><span class="c10">). Customer will pay (or reimburse eXo for the payment of) all Taxes except taxes on eXo&rsquo;s net income, unless Customer provides eXo a valid state sales/use/excise tax exemption certificate or direct pay permit. If Customer is required to pay any withholding tax, charge or levy in respect of any payments due to eXo hereunder, Customer agrees to gross up payments actually made such that eXo shall receive sums due hereunder in full and free of any deduction for any such withholding tax, charge or levy.</span></p>
                    <p class="c1"><span class="c3">4.4 Audit Rights.</span><span class="c10">&nbsp;Customer will maintain accurate records as to its use of the Software as authorized by this Agreement, for at least two (2) years from the last day on which Subscription Plans expired for the applicable Software. eXo, or persons designated by eXo, will, at any time during the period when Customer is obliged to maintain such records, be entitled to audit such records and to ascertain completeness and accuracy, in order to verify that the Software is used by Customer in accordance with the terms of this Agreement and that Customer has paid the applicable Subscription fees for the Software, provided that: (a) eXo may conduct no more than one (1) audit in any twelve (12) month period; (b) any such audit shall be subject to a mutually agreed upon non-disclosure agreement negotiated in good faith and entered into by the parties (including any third party agent eXo may use in connection with such audit); (c) the audit will be conducted during normal business hours; and (d) eXo shall use commercially reasonable efforts to minimize the disruption of Customer&rsquo;s normal business activities in connection with any such audit. eXo, or persons designated by eXo, shall not have physical access to Customer&rsquo;s computing devices in connection with any such audit, without Customer&rsquo;s prior written consent. Customer shall promptly pay to eXo any underpayments revealed by any such audit. Any such audit will be performed at eXo&rsquo;s expense, provided, however, that Customer shall promptly reimburse eXo for the cost of such audit and any applicable fees if such audit reveals an underpayment by Customer of more than five percent (5%) of the Subscription fees payable by Customer to eXo for the period audited.</span></p>
                    <p class="c1"><span class="c3">5. Term, Termination and Expiration.</span></p>
                    <p class="c1"><span class="c3">5.1 Term.</span><span class="c10">&nbsp;Unless otherwise stated in the applicable Order Form, the Term of this Agreement will begin on the Effective Date and will continue for the period set forth in the Order Form. Thereafter, this Agreement will be automatically renewed for additional terms of one (1) year each under the then-current list price and terms in effect at this date, unless notice to the contrary is given in writing ninety (90) days prior to such termination or unless otherwise stated in the applicable Order Form.</span></p>
                    <p class="c1"><span class="c3">5.2 Termination for Cause.</span><span class="c10">&nbsp;Either party may terminate this Agreement for cause if the other party materially breaches, but only by giving the breaching party written notice of termination and specifying in such notice the alleged material breach. The breaching party will have a grace period of thirty (30) days after such notice is served to cure the breach described therein. If the breach is not cured within the foregoing time period, this Agreement will automatically terminate upon the conclusion of such period. Notwithstanding the foregoing, eXo, in its sole discretion, may terminate this Agreement if Customer violates its obligations under Sections 3 and/or 7.</span></p>
                    <p class="c1"><span class="c3">5.3 Effects of Termination. </span><span class="c10">Upon termination of this Agreement for any reason or expiration : &nbsp;(a) any amounts owed to eXo under this Agreement before such termination or expiration will be immediately due and payable; (b) all License rights granted in this Agreement before such termination or expiration and in any Order Form will immediately terminate; (c) Customer must immediately stop all use of the Software; (d) Customer must erase all copies of the Software from Customer&rsquo;s computers, and destroy all copies of the Software and Documentation on tangible media in Customer&rsquo;s possession or control or return such copies to eXo; (e) each party will return to the other party the Confidential Information of the other party that it obtained during the course of this Agreement; and (f) Customer must certify in writing to eXo that it has returned or destroyed such Software and Documentation. </span></p>
                    <p class="c1"><span class="c10">Sections 1, 4.4, 5.3, 6, 7, 8.3, 9 and 11 will survive expiration or termination of this Agreement for any reason.</span></p>
                    <p class="c1"><span class="c3">6. Proprietary Rights.</span></p>
                    <p class="c1"><span class="c3">6.1</span><span class="c10">&nbsp;As between the parties, Customer acknowledges and agrees that the Software, including its sequence, structure, organization, and source code, constitute certain valuable intellectual property rights including copyrights, trademarks, service marks, trade secrets, patents, patent applications, contractual rights of non-disclosure or any other intellectual property or proprietary right, arising of eXo and/or its suppliers. The Software is licensed and not sold to Customer, and no title or ownership to the Software or the intellectual property rights embodied therein passes as a result of this Agreement or any act pursuant to this Agreement. &nbsp;The Software and Documentation are the exclusive property of eXo and/or its suppliers, and all rights, title and interest in and to such not expressly granted to Customer in this Agreement are reserved. eXo owns all copies of the Software, in any form. &nbsp;Nothing in this Agreement will be deemed to grant, under any legal theory, a license under any of eXo&rsquo;s existing or future patents (or the existing or future patents of its suppliers).</span></p>
                    <p class="c1"><span class="c3">6.2</span><span class="c10">&nbsp;Customer acknowledges that in the course of performing any Support Services, eXo may create software or other works of authorship (collectively</span><span class="c3">&nbsp;&ldquo;Work Products&rdquo;</span><span class="c10">). Subject to Customer&rsquo;s rights in the Customer Confidential Information, eXo shall own all right, title and interest in such Work Products, including all intellectual property rights therein and thereto. &nbsp;If any Work Product is delivered to Customer pursuant to or in connection with the performance of Support Services (a </span><span class="c3">&ldquo;Deliverable&rdquo;</span><span class="c10">), eXo hereby grants to Customer a license to such Deliverable under the same terms and conditions Customer&rsquo;s license to Software set forth in Section 3 above.</span></p>
                    <p class="c1"><span class="c3">6.3</span><span class="c10">&nbsp;Customer is not obtaining any intellectual property right in or to any materials, works of authorship, software provided by eXo to Customer in connection with the provision to Customer of Support Services (</span><span class="c3">&ldquo;Materials&rdquo;</span><span class="c2">), other than the rights of use specifically granted in this Agreement. Customer will be entitled to keep and use all Materials provided by eXo to Customer, but without any other license to exercise any of the intellectual property rights therein, all of which are hereby strictly reserved to eXo. In particular and without limitation, Materials may not be copied electronically or otherwise whether or not for archival purposes, modified including translated, re-distributed, disclosed to third parties, lent, hired out, made available to the public, sold, offered for sale, shared, or transferred in any other way. All eXo trademarks, trade names, logos and notices present on the Materials will be preserved and not deliberately defaced, modified or obliterated except by normal wear and tear. Customer shall not use any eXo trademarks without eXo&rsquo;s express written authorization.</span></p>
                    <p class="c1"><span class="c3">6.4</span><span class="c10">&nbsp;Specific Developments :</span><span class="c69">&nbsp;</span><span class="c54">Other than the limited rights specifically granted in this Agreement. eXo will own all right, title, and interest in and to its pre-existing technology, the Software, Support Services, Hosting and Managed Services, and all modifications, enhancements and Work Products thereof, and all associated Intellectual Property Rights. Subject to eXo&rsquo; rights in its pre-existing technology, Customer will own all right, title and interest in and to all Specific Developments, if applicable, and all Intellectual Property Rights associated with such Specific Developments. eXo hereby acknowledges that Specific Developments are works done under the Customer&rsquo;s direction and control and have been specially ordered or commissioned by Customer.</span></p>
                    <p class="c1 c36"><span class="c54"></span></p>
                    <p class="c1"><span class="c3">7. Confidential Information.</span><span class="c10">&nbsp;The term &quot;Confidential Information&quot; shall mean any information disclosed by either party (the </span><span class="c3">&ldquo;Discloser&rdquo;</span><span class="c10">) to the other party (the </span><span class="c3">&ldquo;Recipient&rdquo;</span><span class="c10">) in connection with this Agreement, that is disclosed in writing, orally or by inspection and is identified as &quot;Confidential&quot; or &quot;Proprietary&quot;, or which, under the circumstances surrounding disclosure ought to be treated as confidential by the Recipient. Notwithstanding the foregoing, the following is &quot;Confidential Information&quot; of eXo: Any information, in whatever form, disclosed by eXo that relates to the Software and that is not publicly known. The Recipient shall treat as confidential all Confidential Information received from the Discloser, shall not use such Confidential Information except as expressly permitted under this Agreement, and shall not disclose such Confidential Information to any third party without the Discloser&#39;s prior written consent; provided, however, the Recipient may disclose Confidential Information to its employees and contractors on a need-to-know-basis, who have an agreement with the Recipient that would protect the Discloser to the same extent and which restricts disclosure of the Confidential Information in the same manner as this Agreement. The Recipient is liable for all acts and omissions of its employees and contractors that such act or omission would be a breach of this Agreement if it had been done by the Recipient. The Recipient shall use the same measures to protect the Confidential Information that it takes with its own most confidential information, but in no event less than reasonable measures, to prevent the disclosure and unauthorized use of Confidential Information. Notwithstanding the above, the restrictions of this Section shall not apply to information that: &nbsp;(a) was independently developed by the Recipient without any use of the Confidential Information of the Discloser; (b) becomes known to the Recipient, without restriction, from a third party without breach of this Agreement and who had a right to disclose it; (c) was in the public domain at the time it was disclosed or becomes in the public domain through no act or omission of the Recipient; (d) was rightfully known to the Recipient, without restriction, at the time of disclosure; or (e) is disclosed pursuant to the order or requirement of a court, administrative agency, or other governmental body; provided, however, that the Recipient shall provide prompt notice thereof to the Discloser and shall use its reasonable best efforts to prevent public disclosure of such information. Recipient shall, at Discloser&rsquo;s request, return all originals, copies, reproductions and summaries of Confidential Information and all other tangible materials and devices provided to the Recipient as Confidential Information, or at Discloser&#39;s option, certify destruction of the same.</span></p>
                    <p class="c1"><span class="c3">8. Warranties</span></p>
                    <p class="c1"><span class="c3">8.1 Performance.</span><span class="c10">&nbsp;eXo warrants to Customer that, for a period of thirty (30) days from the Effective Date (</span><span class="c3">&ldquo;Warranty Period&rdquo;</span><span class="c10">), the Software, when used as permitted under this Agreement and in accordance with its Documentation, will operate in substantial conformity with its Documentation. &nbsp;eXo&rsquo;s sole liability (and Customer&rsquo;s sole and exclusive remedy) for any breach of this warranty shall be, in eXo&rsquo;s sole discretion, to replace the non-conforming Software or use commercially reasonable efforts to correct the non-conformity; provided that eXo is notified in writing of such non-conformity within the Warranty Period. This warranty shall not apply if: &nbsp;(i) the Software is used outside the scope of this Agreement or used inconsistently with its Documentation; (ii) the Software is modified or altered in any way except by eXo; or (iii) damages are due to negligence or misuse or abuse of the Software. Any replacement or error correction will not extend the original Warranty Period.</span></p>
                    <p class="c1"><span class="c3">8.2 Support Services and Hosting and Managed Services.</span><span class="c10">&nbsp;The Support Services and/or Hosting and Managed Services shall be deemed &nbsp;accepted by Customer upon delivery. eXo warrants that the Support Services and Hosting and Managed Services to be performed hereunder will be done in a workmanlike manner and shall conform to standards of the industry. eXo&rsquo;s sole liability (and Customer&rsquo;s sole and exclusive remedy) for any breach of this warranty shall be for eXo to re-perform the applicable Services; provided that eXo is notified in writing of such non-conformity is notified and reasoned in writing within three (3) days following the occurrence of a default in the performance of the relevant Services.</span></p>
                    <p class="c68 c99"><span class="c3">8.3 Disclaimer.</span><span class="c10">&nbsp;THE SOFTWARE AND ANY SERVICES PROVIDED HEREUNDER ARE PROVIDED &ldquo;AS IS.&rdquo; EXCEPT FOR THE EXPRESS WARRANTIES PROVIDED IN SECTIONS 8.1 AND 8.2, EXO MAKES NO OTHER WARRANTIES WITH RESPECT TO THE SOFTWARE, SERVICES OR ANY OTHER MATERIAL, INFORMATION OR SERVICES PROVIDED HEREUNDER. eXo hereby disclaims all other warranties of merchantability, fitness for a particular purpose, accuracy, result, effort, title and non-infringement. eXo does not warrant that any software or any services will be provided error free, will operate without interruption or will fulfill any of Customer&rsquo;s particular purposes or needs. Customer acknowledges that IT HAS RELIED ON NO WARRANTIES OTHER THAN THE EXPRESS WARRANTIES SET FORTH IN SECTIONS 8.1 and 8.2 AND THAT NO WARRANTIES ARE MADE BY ANY OF EXO&rsquo;S SUPPLIERS OR DISTRIBUTORS. &nbsp;Customer acknowledges and agrees that the prices offered under this agreement reflect these negotiated warranty provisions. To the extent that eXo cannot disclaim any such warranty as a matter of applicable law. To the avoidance of doubt, in no event will the applicable law has any effect on the limitation of liability set forth section 9.</span></p>
                    <p class="c1"><span class="c3">9. Limitation of Liability.</span><span class="c10">&nbsp;Neither party will be liable to any other party for any indirect, incidental, special, consequential, punitive or exemplary damages arising out of or related to this Agreement under any legal theory, including but not limited to (I) lost profits, lost data or business interruption, even if such party has been advised of, knows of, or should know of the possibility of such damages, AND (ii) ANY CLAIM ATTRIBUTABLE TO ERRORS, OMISSIONS OR OTHER INACCURACIES IN OR DESTRUCTIVE PROPERTIES OF THE SOFTWARE &nbsp;claim attributable to errors, omissions or other inaccuracies in or destructive properties of the software or any Services. Regardless of the cause of action, whether in contract, tort or otherwise, neither party&rsquo;s total cumulative liability for actual damages arising out of or related to this Agreement will exceed the total amount of subscription fees that Customer has paid for the Software or Services giving rise to such liability. NEITHER PARTY SHALL BRING ANY CLAIM BASED ON THE SOFTWARE NOR THE SERVICES PROVIDED HEREUNDER MORE THAN EIGHTEEN (18) MONTHS AFTER THE CAUSE OF ACTIONS ACCRUES. Notwithstanding anything to the contrary herein, the limitations of this Section 9 will not apply to or otherwise limit either party&rsquo;s breach of its obligations of nondisclosure under Section 7 or Customer&rsquo;s breach of the license restrictions in Section 3. The parties acknowledge that this Section 9 reflects the allocation of risk between the parties and that neither party would enter into this Agreement without these limitations on its liability. This Limitation of Liability will apply notwithstanding the failure of essential purpose of any limited remedy set forth herein.</span></p>
                    <p class="c1"><span class="c3">10. Indemnification</span></p>
                    <p class="c1"><span class="c3">10.1 eXo&rsquo;s Obligation</span><span class="c10">. Subject to the remainder of Section 10, eXo will defend Customer against any third party claim that the Software licensed hereunder infringes any copyrights registered or issued as of the Effective Date (</span><span class="c3">&ldquo;Infringement Claim&rdquo;</span><span class="c10">) and indemnify Customer from the resulting costs and damages awarded against Customer to the third party making such Infringement Claim, by a court of competent jurisdiction or agreed to in settlement ; provided that Customer &nbsp;(i) notifies eXo promptly in writing of such Infringement Claim, (ii) grants eXo sole control over the defense and settlement thereof, and (iii) reasonably cooperates in response to an eXo request for assistance. eXo will have the exclusive right to defend any such Infringement Claim and make settlements thereof at its own discretion, and Customer may not settle or compromise such Infringement Claim, except with prior written consent of eXo. &nbsp; </span></p>
                    <p class="c1"><span class="c3">10.2 Cure</span><span class="c10">. Should any Software become, or in eXo&rsquo;s opinion be likely to become, the subject of such an Infringement Claim, eXo shall, at its option and expense, (a) procure for Customer the right to make continued use thereof, (b) replace or modify such so that it becomes non-infringing, or (c) request return of the Software and, upon receipt thereof, the corresponding licenses are terminated and eXo shall refund the price paid by Customer, less straight-line depreciation based on a three (3) year useful life.</span></p>
                    <p class="c1"><span class="c3">10.3 Exclusions</span><span class="c10">. eXo shall have no liability after the termination of the Agreement, and if the alleged infringement is based on (1) combination with non-eXo products, (2) use for a purpose or in a manner for which the Software were not designed, (3) use of any older version of the Software when use of a newer eXo revision would have avoided the infringement, (4) any modification made by anyone other than eXo, (5) any modification made by eXo pursuant to Customer&rsquo;s specific instructions, or (6) any intellectual property right owned or licensed by Customer, excluding the Software.</span></p>
                    <p class="c1"><span class="c3">10.4 Limitation. THIS SECTION STATES CUSTOMER&rsquo;S SOLE AND EXCLUSIVE REMEDY AND EXO&rsquo;S ENTIRE LIABILITY FOR INFRINGEMENT CLAIMS.</span></p>
                    <p class="c1"><span class="c3">11. General</span></p>
                    <p class="c1"><span class="c3">11.1 Notices.</span><span class="c10">&nbsp;All notices under this Agreement must be delivered in writing in person, by courier, or by certified or registered mail (postage prepaid and return receipt requested) to the other party at the address set forth in the applicable Order Form and will be effective upon receipt or three (3) business days after being deposited in the mail as required above, whichever occurs sooner. &nbsp;Either party may change its address by giving written notice of the new address to the other party.</span></p>
                    <p class="c1"><span class="c3">11.2 Relationship of the Parties.</span><span class="c10">&nbsp;The parties hereto are independent contractors. Nothing in this Agreement shall be deemed to create an agency, employment, partnership, fiduciary or joint venture relationship between the parties. Neither party has the power or authority as agent, employee or in any other capacity to represent, act for, bind or otherwise create or assume any obligation on behalf of the other party for any purpose whatsoever. There are no third party beneficiaries to this Agreement.</span></p>
                    <p class="c1"><span class="c3">11.3 Assignments.</span><span class="c10">&nbsp;Customer may not assign or transfer any of its rights or delegate any of its duties under this Agreement (including its licenses with respect to the Software) to any third party unless expressly authorized in writing by eXo. Any other attempted assignment or transfer by Customer in violation of the foregoing will be void. Subject to the foregoing, this Agreement will be binding upon and will inure to the benefit of the parties and their respective successors and assigns.</span></p>
                    <p class="c1"><span class="c3">11.4 Governing Law and Venue.</span><span class="c2">&nbsp;</span></p>
                    <p class="c62"><span class="c3">11.4.1</span><span class="c2">&nbsp;If this Agreement is entered into with eXo Platform SAS, it will be governed by the laws of France and the Paris Courts will have exclusive jurisdiction over any dispute arising out or connected with this Agreement and the French version of the Agreement will prevail.</span></p>
                    <p class="c62"><span class="c3">11.4.2 </span><span class="c2">If this Agreement is entered into with eXo Platform NA LLC, it will be governed by the laws applicable in the state of New York and by the applicable federal laws of United States of America, the New York state Courts will have exclusive jurisdiction over any dispute arising out or connected with this Agreement, and the English version of this Agreement will prevail.</span></p>
                    <p class="c1"><span class="c3">11.5 Marketing Activities.</span><span class="c10">&nbsp;Customer agrees that eXo may from time to time identify Customer (with its name, logo and/or trademark) as an eXo customer in or on its web site, sales and marketing materials or press releases, subject to Customer&rsquo;s trademark and logo usage guidelines provided by Customer.</span></p>
                    <p class="c1"><span class="c3">11.6 Remedies.</span><span class="c10">&nbsp;Except as specifically provided otherwise in this Agreement, the parties&rsquo; rights and remedies under this Agreement are cumulative. Customer acknowledges that the Software contains valuable trade secrets and proprietary information of eXo and that any actual or threatened disclosure or misapplication of such Software or Confidential Information will constitute immediate and irreparable harm to eXo for which monetary damages would be an inadequate remedy and for which eXo will be entitled to seek injunctive relief. &nbsp;If any legal action is brought to enforce this Agreement, the prevailing party will be entitled to receive its attorneys&rsquo; fees, court costs, and other collection expenses, in addition to any other relief it may receive.</span></p>
                    <p class="c1"><span class="c3">11.7 Waivers.</span><span class="c10">&nbsp;All waivers must be in writing. Any waiver or failure to enforce any provision of this Agreement on one occasion will not be deemed a waiver of any other provision or of such provision on any other occasion.</span></p>
                    <p class="c1"><span class="c3">11.8 Severability.</span><span class="c10">&nbsp;If any provision of this Agreement is adjudicated to be unenforceable, such provision will be changed and interpreted to accomplish the objectives of such provision to the greatest extent possible under applicable law and the remaining provisions will continue in full force and effect. Without limiting the generality of the foregoing, Customer agrees that Section 9 will remain in effect notwithstanding the unenforceability of any provision in Section 8.3.</span></p>
                    <p class="c1"><span class="c3">11.9 Force Majeure.</span><span class="c10">&nbsp;Except for Customer&#39;s obligations to pay eXo hereunder, neither party shall be liable to the other party for any failure or delay in performance caused by reasons beyond its reasonable control to the extent the occurrence is caused by fires, floods, epidemics, famines, earthquakes, hurricanes and other natural disasters or acts of God, regulation or acts of any civilian or military authority or act of any self-regulatory authority, wars, terrorism, riots, civil unrest, sabotage, theft or other criminal acts of third parties.</span></p>
                    <p class="c1"><span class="c3">11.10 Entire Agreement.</span><span class="c10">&nbsp;This Agreement (including each Order Form, and attachment thereto) constitutes the entire agreement between the parties regarding the subject hereof and supersedes all prior or contemporaneous agreements, understandings and communications, whether written or oral. This Agreement may be amended only by a written document signed by both parties. The terms of this Agreement will control over any conflicting provisions in an Order Form or any standard terms and conditions set forth on either party&rsquo;s form documents, including any purchase order or click-through agreement contained on a Web site and any conflicting terms in any &ldquo;click-to-accept&rdquo; end user license agreement that may be embedded within the Software, except for terms regarding Third-Party Software which are incorporated herein by reference under Section 3.4 (&ldquo;Third-Party Software&rdquo;).</span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c25"></span></p>
                    <p class="c26"><span class="c25"></span></p>
                    <p class="c40"><span class="c0">Appendix 0: Subscription Plans</span></p>
                    <p class="c32 c68 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c2">During the time that Customer has paid the applicable annual Subscription fees, Customer will receive access to (a) the applicable Software via an official download location provided by eXo, (b) a Licence key allowing for unlocking the Software runtime, (c) the applicable Software Updates, when and if available, via an official download location provided by eXo, (d) the applicable level of Support Services as described in Appendix 1 and (e) the applicable level of Hosting and Management Services as described in Appendix 3.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">1. Additional Definitions</span></p>
                    <p class="c1"><span class="c0">1.1 Installed Systems</span></p>
                    <p class="c1"><span class="c10">For purposes of the Subscription plans described in this Appendix, the term &ldquo;Installed Systems&rdquo; refers to installations of the Software on one or several physical or virtual servers or workstations running a tier limit of CORE Processors (e.g., up to 64 or up to 256) or a tier limit of Registered Users (e.g, up to 25 or up to 1000). &nbsp;</span><span class="c15">Each Installed System running the Software must be covered by an active Subscription plan. eXo reserves all rights to change the tier limits from time to time. Such change will be notified to Customer through a written notice issued no less than 60 (sixty) days before the change.</span></p>
                    <p class="c1"><span class="c0">1.2 Unit Count</span></p>
                    <p class="c1"><span class="c15">For the avoidance of doubt, and unless specified in an applicable Order Form, all Installed Systems count toward the limit of CORE Processors band or Registered Users tier purchased through a Subscription Plan, regardless of their actual usage. </span></p>
                    <p class="c1"><span class="c6">1.3 Authorized deployment topology</span></p>
                    <p class="c1"><span class="c15">Subscription Plans may introduce benefits and limitations regarding the type of deployment allowed to Customer. (a) A Single Server deployment means an Installed System hosting the Software in a single Java Virtual Machine. (b) A Multi Server deployment refers to Installed Systems hosting the Software in multiple Java Virtual Machines. (c) A High Availability deployment refers to Installed Systems hosting the Software in multiple Java Virtual Machines communicating together to ensure better scaling capabilities and performance.</span></p>
                    <p class="c1"><span class="c6">1.4 Software Package Deliverable</span></p>
                    <p class="c1"><span class="c15">Depending on the Subscription Plan purchased by Customer, eXo will deliver to Customer the Software Package according to the following available distributions : </span></p>
                    <p class="c1"><span class="c13">1.4.1 The Official Software Installer </span><span class="c69">is a distribution package allowing Customer to install the Software, relevant Official Add-ons and a predefined middleware stack necessary to execute the Software Binary code in a single Production environment. Changes and/or modifications and/or disruption and/or augmentation by any means including specific code or integration to third parties software of the components installed by the Official Software Installer are not allowed and constitute a material breach of the this agreement.</span></p>
                    <p class="c1"><span class="c13">1.4.2 The Official Binary Software Archive </span><span class="c15">is a distribution package of the Software dedicated to be installed by Customer within its own server environment. &nbsp;This distribution does not include any third party middleware components therefore it remains the sole responsibility of Customer to provide and install them if they&rsquo;re not already in place.</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c0">2. Subscription Plans details</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c2">Unless specified in an applicable Order Form, purchasing a Subscription Plan will entitle Customer to benefit from the following items : </span></p>
                    <p class="c1"><span class="c69">(V means available, X means Not available for this tier or plan)</span></p>
                    <p class="c1 c36"><span class="c6"></span></p>
                    <a id="t.3f93fd52e49cc51b7bdcdf4d2306bfd7513ff8fe"></a>
                    <a id="t.0"></a>
                    <table class="c117">
                        <tbody>
                            <tr class="c85">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c1 c36"><span class="c6"></span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">&ldquo;Professional&rdquo; Subscription</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">&ldquo;Enterprise&rdquo; Subscription</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">&ldquo;Enterprise Plus&rdquo; Subscription</span></p>
                                </td>
                            </tr>
                            <tr class="c43">
                                <td class="c51" colspan="4" rowspan="1">
                                    <p class="c4"><span class="c6 c58">Software</span></p>
                                </td>
                            </tr>
                            <tr class="c43">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Access to certified Production-ready Software</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                            </tr>
                            <tr class="c43">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">Software Package Deliverable</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">X (Hosted only)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Official Binary Software archive</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Official Binary Software archive</span></p>
                                </td>
                            </tr>
                            <tr class="c43">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">Authorized Official Software Add-ons</span></p>
                                </td>
                                <td class="c95" colspan="3" rowspan="1">
                                    <p class="c4"><span class="c2">Listing available in Appendix 4</span></p>
                                </td>
                            </tr>
                            <tr class="c96">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Multi-year Software Support Lifecycle policy, &nbsp;Software certified updates, patches and bug fixes through Maintenance program </span><span class="c10">(As defined in Appendix 1) (*)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Access to Documentation and technical guides </span><span class="c10">(administration &amp; usage, Installation, development)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">V</span></p>
                                </td>
                            </tr>
                            <tr class="c106">
                                <td class="c51" colspan="4" rowspan="1">
                                    <p class="c4"><span class="c6 c58">Services</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">Access to Customer Success Program</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Access to Support Services </span><span class="c10">(As defined in Appendix 1)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Help Desk </span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Help Desk</span></p>
                                    <p class="c4"><span class="c2">Technical Support</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Help Desk</span></p>
                                    <p class="c4"><span class="c2">Technical Support</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">Support Services SLA Guidelines</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Standard SLA</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Standard SLA</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Standard or Premium SLA</span></p>
                                </td>
                            </tr>
                            <tr class="c78">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Hosting and Managed Services Available </span><span class="c2">(**)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">&ldquo;Professional&rdquo; Hosting</span></p>
                                    <p class="c4"><span class="c2">Hosting Support</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">&ldquo;Enterprise&rdquo; Hosting</span></p>
                                    <p class="c4"><span class="c2">Hosting Support</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4" id="h.30j0zll"><span class="c2">&ldquo;Enterprise Plus&rdquo; Hosting </span></p>
                                    <p class="c4"><span class="c2">Hosting Support</span></p>
                                </td>
                            </tr>
                            <tr class="c78">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Professional Services Available </span><span class="c10">(***)</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Consulting</span></p>
                                    <p class="c4"><span class="c2">Training</span></p>
                                    <p class="c4 c36"><span class="c21"></span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Consulting</span></p>
                                    <p class="c4"><span class="c2">Training</span></p>
                                    <p class="c4"><span class="c2">Specific Developments</span></p>
                                    <p class="c4 c36"><span class="c21"></span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Consulting</span></p>
                                    <p class="c4"><span class="c2">Training</span></p>
                                    <p class="c4"><span class="c10">Specific Developments</span></p>
                                </td>
                            </tr>
                            <tr class="c78">
                                <td class="c51" colspan="4" rowspan="1">
                                    <p class="c4"><span class="c6 c58">Installed Systems allowance</span></p>
                                </td>
                            </tr>
                            <tr class="c78">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Applicable Unit</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Registered User tier</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Registered User tier</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Core Processor band</span></p>
                                </td>
                            </tr>
                            <tr class="c65">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Registered users number</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Limited</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Limited</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Unlimited</span></p>
                                </td>
                            </tr>
                            <tr class="c65">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">Authorized production deployment topology</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Single Server </span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Single Server </span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Single Server</span></p>
                                    <p class="c4"><span class="c10">Multi Server <br>High Availability</span></p>
                                </td>
                            </tr>
                            <tr class="c65">
                                <td class="c24" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">Authorized project deployment topology</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">None</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">One (1) Single Server Sandbox</span></p>
                                    <p class="c4"><span class="c2">Two (2) Developer Workstations</span></p>
                                </td>
                                <td class="c11" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No further restriction than the Subscribed units of Core Processor Bands. (****).</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c21"></span></p>
                    <p class="c1"><span class="c2">(*) Extended Lifetime Support is available under nonstandard conditions as defined in an applicable Order Form. For the avoidance of doubt, no Extended Lifetime Support is included in any Subscription Plan and all Extended Lifetime Support offers require a baseline existing Subscription Plan as defined in this agreement.</span></p>
                    <p class="c1"><span class="c2">(**) Hosting and Managed Services are sold as an option to a Subscription Plan. Such option must be specified in an applicable Order Form. Hosting and Managed Services terms are defined in Appendix 3.</span></p>
                    <p class="c1"><span class="c2">(***) Unless otherwise specified in an applicable Order Form, Subscription plans never include any Professional Services by default but rather enable their purchase. Professional Services terms are defined in Appendix 2.</span></p>
                    <p class="c1"><span class="c2">(****) Unless otherwise specified in an applicable Order Form, the Unit Count for the Enterprise Plus plan is performed thanks to the following rule : (1) Server environments (Production - Pre-production - Homologation - Acceptance - Backup - Cold Backup) all account for their corresponding CORE unit count. (2) Developer workstations each account for One (1) CORE regardless of their actual power.</span></p>
                    <p class="c32 c68 c36"><span class="c25"></span></p>
                    <p class="c32 c68"><span class="c3">3. Current Subscription Plans</span></p>
                    <p class="c32 c68 c36"><span class="c25"></span></p>
                    <p class="c1"><span class="c0">3.1. General Information in Commercial Offers</span></p>
                    <p class="c1"><span class="c2">The Subscription Plans may vary in time at eXo&rsquo;s sole discretion. eXo does not guarantee the permanent availability of a Subscription Plan and its associated benefits and limitations. Any Subscription Plan acquired by the Customer remains however valid during the initial Term indicated in the applicable Order Form, including its benefits and limitations. At the end of this period, the renewal terms set out in article 4.2 of the Master Subscription Agreement apply fully, without recourse or compensation for the Client.</span></p>
                    <p class="c1"><span class="c2">The catalog of commercial offers available for purchase to date through a Subscription Plan is included only in this appendix.Any other source, publication, reseller or distributor&rsquo;s catalog, advertising by eXo or a third party cannot be opposed to eXo for any remedy. </span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">3.2 Official Subscription Plans Catalog</span></p>
                    <p class="c1"><span class="c2">The following table lists the references and official designation of eXo Subscription Plans available for purchase, to date.</span></p>
                    <a id="t.a636e95b85060f9f6bc7f529b038c6ac1956649b"></a>
                    <a id="t.1"></a>
                    <table class="c91">
                        <tbody>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">SKU</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">Detail</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Enterprise Plus Edition</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM64-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Premium Support 64 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM32-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Premium Support 32 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM16-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Premium Support 16 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD64-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 64 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD32-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 32 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD16-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 16 Cores - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM64-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Premium Support 64 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM32-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Premium Support 32 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-PREM16-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus- Premium Support 16 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD64-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 64 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD32-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 32 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-STD16-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Standard Support 16 Cores &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c74">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Enterprise Plus Edition including an Hosting and Managed Services Option</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-HOS-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus- Hosted x users - 1 Year (la quantit&eacute; d&rsquo;Utilisateurs est sp&eacute;cifi&eacute; dans le Formulaire d&rsquo;Engagement Applicable)</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTPLUS-HOS-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Plus - Hosted x users - 3 Years (la quantit&eacute; d&rsquo;Utilisateurs est sp&eacute;cifi&eacute; dans le Formulaire d&rsquo;Engagement Applicable)</span></p>
                                </td>
                            </tr>
                            <tr class="c74">
                                <td class="c58 c70" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Enterprise Edition</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-300-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 300 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-400-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 400 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-500-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 500 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-600-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 600 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">R&eacute;f&eacute;rence</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">D&eacute;signation</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-700-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 700 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-800-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 800 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-900-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 900 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-1000-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 1000 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-300-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 300 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-400-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 400 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-500-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 500 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-600-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 600 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-700-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 700 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-800-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 800 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-900-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 900 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENT-1000-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise &ndash; Up To 1000 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c74">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Enterprise Edition including an Hosting and Managed Services Option</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-300-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 300 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-400-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 400 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-500-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 500 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-600-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 600 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-700-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 700 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-800-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 800 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-900-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 900 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-1000-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 1000 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-300-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 300 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">R&eacute;f&eacute;rence</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c53"><span class="c0">D&eacute;signation</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-400-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 400 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-500-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 500 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-600-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 600 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-700-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 700 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-800-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 800 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-900-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 900 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">ENTHOS-1000-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Enterprise Hosted &ndash; Up To 1000 users - 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c74">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Professional Edition</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-25-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 25 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-50-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 50 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-100-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 100 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-200-1Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 200 users - 1 Year</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-25-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 25 users &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-50-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted &ndash; Up To 50 users &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-100-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 100 users &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">PROHOS-200-3Y</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">eXo Platform Professional Hosted&ndash; Up To 200 users &ndash; 3 Years</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c53"><span class="c6 c58">Extended LifeCycle Support (Only applicable to Enterprise and Enterprise Plus Edition)</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">EXTSUP-STD-6M</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">Standard Support - Extended Lifecycle - 6 Months (requires an active Subscription Plan)</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c35" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">EXTSUP-PREM-6M</span></p>
                                </td>
                                <td class="c33" colspan="1" rowspan="1">
                                    <p class="c32"><span class="c2">Premium Support - Extended Lifecycle - 6 Months (requires an active Subscription Plan)</span></p>
                                </td>
                            </tr>
                            <tr class="c19">
                                <td class="c70 c58" colspan="2" rowspan="1">
                                    <p class="c32 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1 c36"><span class="c25"></span></p>
                    <p class="c26"><span class="c21"></span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c21"></span></p>
                    <p class="c26"><span class="c21"></span></p>
                    <p class="c27"><span class="c0">Appendix 1: Support Services</span></p>
                    <p class="c1"><span class="c3">1.1 Support Services entitlement</span></p>
                    <p class="c1"><span class="c2">The Support Services are intended only for use by Customer (including through its contractors and agents) and for the benefit of the Customer and only for the Installed Systems (as defined below) for which Customer has purchased a Subscription Plan. Any unauthorized use of the Support Services will be deemed to be a material breach of this Agreement</span></p>
                    <p class="c1"><span class="c3">1.2 Support Services Start Date</span></p>
                    <p class="c1"><span class="c2">Unless otherwise agreed in an Order Form, the Support Services will begin on the date Customer purchases a Subscription Plan as set forth in the applicable Order Form.</span></p>
                    <p class="c1"><span class="c0">1.3 Support Services Scope of Coverage</span></p>
                    <p class="c1"><span class="c2">eXo Support Services are intended to provide functional and/or technical assistance, troubleshooting competence and Software Maintenance to Customer. The benefits of these Support Services vary according to the Subscription Plan purchased by the Customer (See Subscription Plan Details in Appendix 0).</span></p>
                    <p class="c1"><span class="c2">Support Services are delivered remotely through an industrialized organization and SLA-driven processes. Customer may formulate requests through the support portal made available by eXo.</span></p>
                    <p class="c1"><span class="c10">Support services are provided in the English and/or French languages only. eXo will respond according to SLAs as defined hereafter. eXo and Customer will specify initial contacts, </span><span class="c94 c10">This information may be changed upon request by the Client during the period of validity of the Subscription</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c2">For the avoidance of doubt and unless specified in an applicable Order Form, Support Services never include any on-site intervention. Such interventions shall be scoped and ordered through additional Professional Services. </span></p>
                    <p class="c1"><span class="c10">Support Services do not include assistance with code development, system and/or network design, architectural design, major Releases changes for the Software or for third party software made available with the Software. </span><span class="c10 c94">Such interventions will be scoped jointly by eXo and the Customer and ordered through additional Consulting Services.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c2">eXo does not provide Support Services for Software that has been modified nor for Customer specific developments.</span></p>
                    <p class="c1"><span class="c10">Support Services are provided only according to the Supported Environments Policy listed at <a href="https://www.exoplatform.com/supported-environments">https://www.exoplatform.com/supported-environments</a>. Support Services are not provided on environments outside of the scope referenced above. </span></p>
                    <p class="c1"><span class="c2">Depending on the Subscription Plan purchased by Customer, Customer has access to several services and escalations:</span></p>
                    <p class="c1"><span class="c6">1.3.1 Help Desk</span></p>
                    <p class="c1"><span class="c69">Help Desk is the primary entry point for Customer requests. All Customer requests made within the scope of Support Services must be initiated at the Help Desk. Help Desk support consists in </span><span class="c10">administrative acknowledgement of support requests, the formulation of recommendations for use and functional administration, case management, basic troubleshooting, and providing common solutions</span><span class="c15">&nbsp;to functional-related questions. </span></p>
                    <p class="c1"><span class="c13">1.3.2 Technical Support&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></p>
                    <p class="c1"><span class="c2">Technical Support consists of assistance for installation, configuration, technical administration, advanced troubleshooting, issue diagnosis and reproduction and the provision of technical workarounds and procedures to be performed by Customer on the applicable Software in the context of an Client-hosted and/or managed infrastructure, n an Authorized Deployment Topology. </span></p>
                    <p class="c1"><span class="c0">1.3.3 Hosting Support</span></p>
                    <p class="c1"><span class="c2">Hosting Support consists of a technical assistance dedicated to Customers who purchased an Hosting and Managed Services Option. Hosting Support includes assistance for issue diagnosis, reproduction and resolution and Task Orders operations (as defined in Appendix 3 &ndash; Hosting and Managed Services).</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">1.3.4 Maintenance Program</span></p>
                    <p class="c1"><span class="c10">During the validity period of a Subscription Plan purchased by Customer, and in accordance to any special conditions set forth in the Order Form, eXo shall provide to Customer certified patches and Updates for the installed Software (including any related Documentation). Maintenance benefits are ruled by the eXo maintenance program visible at <a href="https://www.exoplatform.com/maintenance-program">https://www.exoplatform.com/maintenance-program</a>.</span></p>
                    <p class="c1"><span class="c2">eXo does not provide Maintenance for Software that has been modified nor for Customer specific development.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c3">2. Support Services </span><span class="c6">benefits summary by Subscription Plan</span></p>
                    <p class="c1"><span class="c15">Unless specified in an applicable Order Form, and depending on the target deployment option (On-premise or Hosted) purchasing a Subscription Plan entitles Customer to receive Support Services according to the following benefits:</span></p>
                    <p class="c1"><span class="c15">(V means available, X means Not available for this tier or plan)</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <p class="c1"><span class="c6">2.1 On-premise Installations</span></p>
                    <a id="t.c3bdc378f364e7feae682d00818006b350a7ac85"></a>
                    <a id="t.2"></a>
                    <table class="c98">
                        <tbody>
                            <tr class="c5">
                                <td class="c115" colspan="3" rowspan="1">
                                    <p class="c4"><span class="c0">Support Services Benefits for On-Premise installation</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c75" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c6">Subscription Plan</span></p>
                                </td>
                                <td class="c57" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Enterprise</span></p>
                                </td>
                                <td class="c18" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Enterprise Plus</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">SLA guideline</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Standard </span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">Standard or Premium</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c6">Ongoing support</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">X</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Case Management </span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Support channel</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Web</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Web and Phone</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Number of named contacts</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">3</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">5</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Help Desk (Level 1)</span></p>
                                </td>
                                <td class="c48" colspan="2" rowspan="1">
                                    <p class="c4 c36"><span class="c21"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Usage information</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Basic troubleshooting </span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Functional administration assistance</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Technical Support (Level 2)</span></p>
                                </td>
                                <td class="c48" colspan="2" rowspan="1">
                                    <p class="c7 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Installation and configuration instructions</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Technical administration assistance</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Advanced troubleshooting</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Issue diagnosis and reproduction </span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Workarounds and procedures</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Maintenance Program (Level 3)</span></p>
                                </td>
                                <td class="c48" colspan="2" rowspan="1">
                                    <p class="c4 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c6">Maintenance versions</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c6">Cumulative patches versions</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c6">One-off patches</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c21"></span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c6">2.2 Hosted Installations</span></p>
                    <a id="t.08635c422fb7a739ed694359e296ca63ff03e35c"></a>
                    <a id="t.3"></a>
                    <table class="c98">
                        <tbody>
                            <tr class="c5">
                                <td class="c100" colspan="4" rowspan="1">
                                    <p class="c4"><span class="c0">Support Services Benefits for Hosted installations</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c75" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c6">Subscription Plan</span></p>
                                </td>
                                <td class="c56 c83" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Professional</span></p>
                                </td>
                                <td class="c57" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Enterprise</span></p>
                                </td>
                                <td class="c18" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Enterprise Plus</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">SLA guideline</span></p>
                                </td>
                                <td class="c56" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Standard</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Standard </span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">Standard or Premium</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">Ongoing support</span></p>
                                </td>
                                <td class="c56" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">X</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">X</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Case Management</span></p>
                                </td>
                                <td class="c56" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Support channel</span></p>
                                </td>
                                <td class="c56" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Web</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Web</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Web and Phone</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Number of named Contacts</span></p>
                                </td>
                                <td class="c56" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">1</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">3</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">5</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Help Desk (Level 1)</span></p>
                                </td>
                                <td class="c83 c107" colspan="3" rowspan="1">
                                    <p class="c4 c36"><span class="c21"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Usage information</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Basic troubleshooting </span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Functional administration assistance</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Hosting Support (Level 2)</span></p>
                                </td>
                                <td class="c89 c83" colspan="3" rowspan="1">
                                    <p class="c7 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Issue diagnosis and reproduction </span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Task order Operations</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c60 c83" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c13">Maintenance Program (Level 3)</span></p>
                                </td>
                                <td class="c83 c89" colspan="3" rowspan="1">
                                    <p class="c4 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">Maintenance versions</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">Cumulative patches versions</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c47" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c13">One-off patches</span></p>
                                </td>
                                <td class="c82" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">X</span></p>
                                </td>
                                <td class="c50" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                                <td class="c44" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c69">V</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c32 c68"><span class="c3">3. Support Services </span><span class="c6">SLA Guidelines</span></p>
                    <p class="c32 c68 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c3">3.1 Support Services escalation </span></p>
                    <p class="c1"><span class="c69">Within the course of a Customer request to eXo Support Services, a case will be handled thanks to the following escalation procedure :</span></p>
                    <ul class="c23 lst-kix_list_2-0 start">
                        <li class="c1 c46"><span class="c3">Level One Support</span><span class="c2">&nbsp;means administrative and technical acknowledgement of support requests, documentation of requests, basic troubleshooting, and providing common solutions for issues requiring less than 30 minutes to resolve. If no solution is found, a Level Two escalation is made.</span></li>
                        <li class="c1 c46"><span class="c3">Level Two Support</span><span class="c2">&nbsp;means the escalation point for Level One Support. Level Two Support provides support for issues requiring more than thirty (30) minutes to resolve, in-depth research and troubleshooting. All requests reporting one or more Errors with known solutions are Level One and Two Support issues.</span></li>
                        <li class="c1 c46"><span class="c3">Level Three Support</span><span class="c2">&nbsp;means acknowledgement of a category of Errors reported by eXo for the Software which, after initial analysis is determined most likely to be the result of a design defect with the Software or the result of a complex interaction that requires a bug fix as described in the eXo Software maintenance program.</span></li>
                    </ul>
                    <p class="c1"><span class="c3">3.2 Incident Response by Severity </span></p>
                    <p class="c1"><span class="c10">Incident severity levels (defined below) are utilized in establishing the incident impact to the Customer upon incident receipt and will be used to set mutual expectations between Customer and eXo. Severities are established by eXo in accordance with the Severity Levels definitions below and are subject to change during the progress of each specific case.</span></p>
                    <p class="c1"><span class="c3">3.3 Mutual obligations</span></p>
                    <p class="c1"><span class="c10">To help ensure a smooth interaction during the processing of a case, and especially for those with a high severity, it is essential that all parties remain engaged until the case is resolved or qualified with a lower severity level. This includes but is not limited to:</span></p>
                    <ul class="c23 lst-kix_list_1-0 start">
                        <li class="c49"><span class="c10">Provision to eXo Support Services staff of all relevant contact informations as well as all technical activities in progress, runtime logs, technical parameters, and all information available and relevant allowing for a deep analysis of the situation.</span></li>
                        <li class="c49"><span class="c10">Execution of recommendations and procedures emitted by eXo Support Services </span><span class="c69">staff.</span></li>
                    </ul>
                    <p class="c1"><span class="c3">3.4 Support Services and Software Delivery </span></p>
                    <p class="c1"><span class="c10">Unless otherwise set forth in an applicable Order Form, (1) eXo will be the primary source for communication with Customer for requests that are covered by Support Services and (2) the Software Updates, when and if available,</span><span class="c69">&nbsp;will be delivered to Customer via eXo </span><span class="c10">Customer Porta</span><span class="c15">l. </span></p>
                    <p class="c1"><span class="c2">Customer shall designate the number of named contacts as stated in the applicable Subscription Plan or in an applicable Order Form. </span></p>
                    <p class="c1"><span class="c10">In addition to and for purposes of assessing the quality of the service provided, Customer will provide data, and other relevant information upon eXo&rsquo;s request. </span></p>
                    <p class="c1"><span class="c13">3.5 Ongoing Support</span></p>
                    <p class="c1"><span class="c15">Provided Customer has purchased an eligible Subscription Plan or as specified in an applicable Order form, and in case of a Severity 1 issue (see Severity Levels definitions below) eXo Support Services staff will remain engaged on an ongoing basis until case resolution or Severity decreases.</span></p>
                    <p class="c1"><span class="c3">3.6 Support Services SLA Guidelines</span></p>
                    <p class="c1"><span class="c2">eXo will use commercially reasonable efforts to provide Support Services in accordance with the Services Level Agreement (SLA) guidelines set forth in Table below. eXo&#39;s Support Services standard business hours (&quot;Standard Business Hours&quot;) are 8h-18h GMT, from Monday to Friday, excluding regular bank holidays.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c32 c68"><span class="c0">Table: Support Services SLA Guidelines</span></p>
                    <p class="c1"><span class="c69">(V means available, X means Not available for this tier or plan)</span></p>
                    <a id="t.f6c2ce262325574afd7b760332bd4ce823274414"></a>
                    <a id="t.4"></a>
                    <table class="c98">
                        <tbody>
                            <tr class="c59">
                                <td class="c109" colspan="3" rowspan="1">
                                    <p class="c4"><span class="c0">Target Response Time</span></p>
                                </td>
                            </tr>
                            <tr class="c59">
                                <td class="c90" colspan="1" rowspan="1">
                                    <p class="c1 c36"><span class="c0"></span></p>
                                </td>
                                <td class="c87" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Standard</span></p>
                                </td>
                                <td class="c87" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Premium</span></p>
                                </td>
                            </tr>
                            <tr class="c84">
                                <td class="c90" colspan="1" rowspan="1">
                                    <p class="c1"><span class="c3">Severity 1 (Blocker): </span><span class="c10">An Error which severely impacts Customer&rsquo;s production environment (such as loss of production data) or in which Customer&rsquo;s production systems are not functioning. The situation halts Customer&rsquo;s business operations, and no procedural workaround exists.</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">1 Business Hour</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">1 hour on a 24x7 basis</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c90" colspan="1" rowspan="1">
                                    <p class="c1"><span class="c3">Severity 2 (Major): </span><span class="c10">An Error where Customer&rsquo;s system is functioning but in a severely reduced capacity. The situation is causing a high impact to portions of Customer&rsquo;s business operations, and no procedural workaround exists.</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">4 Business Hours</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">4 Hours on a 24x7 basis</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c90" colspan="1" rowspan="1">
                                    <p class="c1"><span class="c3">Severity 3 (Minor): </span><span class="c10">An Error which involves partial, non-critical functionality loss of a production or development system. There is a medium-to-low impact on Customer&rsquo;s business, but Customer&rsquo;s business continues to function, including by using a procedural workaround.</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">2 Business Days</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">1 Business Day</span></p>
                                </td>
                            </tr>
                            <tr class="c55">
                                <td class="c93" colspan="1" rowspan="1">
                                    <p class="c1"><span class="c3">Severity 4 (None/Info): </span><span class="c10">A general usage question, reporting of a documentation error or recommendation for a future product enhancement or modification. There is low-to-no impact on Customer&rsquo;s business or the performance or functionality of Customer&rsquo;s system.</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">3 Business Days</span></p>
                                </td>
                                <td class="c38" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">2 Business Days</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c21"></span></p>
                    <p class="c1"><span class="c3">3.7 Support Services Operational Processes</span></p>
                    <p class="c1"><span class="c10">Support Services detailed operational Processes are available at <a href="https://www.exoplatform.com/terms-conditions/global-support-services.pdf">https://www.exoplatform.com/terms-conditions/global-support-services.pdf</a></span></p>
                    <p class="c32 c68 c36"><span class="c25"></span></p>
                    <p class="c26"><span class="c21"></span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c21"></span></p>
                    <p class="c68 c113"><span class="c3">Appendix 2: Professional Services</span></p>
                    <p class="c36 c52"><span class="c21"></span></p>
                    <p class="c62"><span class="c2">eXo provides services exclusively to customers covered by a valid enterprise subscription plan. Terms and conditions governing eXo&rsquo;s Professional Services delivery processes are stated below. </span></p>
                    <p class="c62"><span class="c0">1. eXo Consulting Services</span></p>
                    <p class="c64"><span class="c0">1.1. Scope of appliance</span></p>
                    <p class="c1"><span class="c10">eXo consulting services (&ldquo;Consulting Services&rdquo;) are meant to provide assistance to Customer on activities involving the Software such as :</span></p>
                    <ul class="c23 lst-kix_list_6-0 start">
                        <li class="c67"><span class="c10">Technical and functional project design and specifications,</span></li>
                        <li class="c67"><span class="c69">User Onboarding and collaborative practices adoption,</span></li>
                        <li class="c67"><span class="c10">Migration and Updates</span></li>
                        <li class="c67"><span class="c10">IT operations and expertise,</span></li>
                        <li class="c67"><span class="c10">On-site technical or functional POCs (Proof Of Concept)</span></li>
                    </ul>
                    <p class="c1"><span class="c10">The Consulting Services do not include any Subscription Plan.</span></p>
                    <p class="c64 c36"><span class="c0"></span></p>
                    <p class="c64"><span class="c0">1.2. Performance and Deliverables</span></p>
                    <p class="c64"><span class="c10">eXo agrees to provide the Consulting Services and Deliverables specified in an Order Form on the terms and conditions of this Appendix and in accordance with the requirements, Deliverables description and delivery dates in such Order Form. eXo shall perform the Consulting Services professionally and diligently and will use its commercially reasonable efforts to perform the Consulting Services in a timely manner. </span></p>
                    <p class="c1"><span class="c10">eXo and the Customer agree to scope the amount and nature of Consulting Services needed in accordance with the involved project.</span></p>
                    <p class="c1"><span class="c10">Consulting Services available scoping unit is the &ldquo;men.day&rdquo; and will depend on the seniority of the human resource profile qualified by eXo to match the requirements of the Customer. </span></p>
                    <p class="c64 c36"><span class="c0"></span></p>
                    <p class="c64"><span class="c0">1.3. Responsibilities</span></p>
                    <p class="c64"><span class="c10">As long as Consulting Services are a time-and-materials commitment, those are meant to be delivered under the operational responsibility of the Customer.</span></p>
                    <p class="c1"><span class="c10">eXo agrees to provide the Customer with reasonable advance notice in the event eXo expects a failure on its part to satisfy a delivery date specified in an Order Form.</span></p>
                    <p class="c64 c36"><span class="c0"></span></p>
                    <p class="c64"><span class="c0">1.4 Ordering, invoicing and Payment</span></p>
                    <p class="c64"><span class="c10">eXo&rsquo;s Consulting Services are meant to be ordered prior to delivery and paid upon the timeframe specified in the related Order Form.</span></p>
                    <p class="c1"><span class="c10">Unless stated in the applicable Order Form, the delivery shall then occur within a timeframe of three (3) months, starting on the effective date of the related Order Form. Any remaining amount of Consulting Services past those three months will be lost and invoiced as if delivered.</span></p>
                    <p class="c1"><span class="c2">The Consulting Services will be agreed to be received upon the presentation of a timesheet by eXo to the Customer, which document will be shared between service managers on a monthly basis.</span></p>
                    <p class="c1"><span class="c10">Unless specified in the Order Form, additional fees such as transport, lodging, third party materials including software licenses, hardware, training, documentation needed to fulfill the delivery requirements are excluded from the scope of the delivery and shall be provided or ordered separately by the Customer.</span></p>
                    <p class="c1"><span class="c0">2. eXo Training Services</span></p>
                    <p class="c1"><span class="c0">2.1. Scope of appliance</span></p>
                    <p class="c1"><span class="c10">eXo&rsquo;s Training Services are meant to provide training to Customer human resources under the following scope :</span></p>
                    <ul class="c23 lst-kix_list_3-0 start">
                        <li class="c34"><span class="c10">&ldquo;Training Services&rdquo; means eXo&rsquo;s training courses, including eXo&rsquo;s publicly available courses and courses provided at a site designated by the Customer</span></li>
                        <li class="c34"><span class="c10">The Training Services do not include any additional Subscription Plan.</span></li>
                    </ul>
                    <p class="c1"><span class="c0">2.2. Performance and Deliverables</span></p>
                    <p class="c1"><span class="c10">eXo agrees to provide the Training Services and Deliverables specified in an Order Form on the terms and conditions of this Appendix and in accordance with the requirements, Deliverables description and delivery dates in such Order Form. </span></p>
                    <p class="c1"><span class="c10">eXo and the Customer agree to scope the amount and nature of Training Services needed in accordance with the involved project.</span></p>
                    <p class="c1"><span class="c0">2.3. Equipment and Facilities</span></p>
                    <p class="c1"><span class="c10">For on-site courses, Customer will supply the facility and equipment as set forth in the Order Form. If eXo agrees to provide the training facilities and hardware, Customer will be liable for any loss or destruction of this equipment and hardware used in connection with the Training Services.</span></p>
                    <p class="c1"><span class="c0">2.4. Customer Responsibilities</span></p>
                    <p class="c1"><span class="c10">Customer is responsible for assessing each participant&rsquo;s suitability for the Training Services, enrollment in the appropriate course(s) and Customer&rsquo;s participants&rsquo; attendance at scheduled courses.</span></p>
                    <p class="c1"><span class="c0">2.5. Rights to Training Materials</span></p>
                    <p class="c1"><span class="c10">All intellectual property embodied in the training products, materials, methodologies, software and processes, provided in connection with the Training Services or developed during the performance of the Training Services (collectively, the </span><span class="c3">&ldquo;Training Materials&rdquo;</span><span class="c10">) are the sole property of eXo and are copyrighted by eXo unless otherwise indicated. Training Materials are provided solely for the use of the participants and may not be copied or transferred without the prior written consent of eXo. Training Materials are eXo&rsquo;s confidential and proprietary information.</span></p>
                    <p class="c1"><span class="c0">2.6. Ordering, invoicing and Payment</span></p>
                    <p class="c1"><span class="c10">eXo&rsquo;s Training Services are meant to be ordered and paid prior to delivery.</span></p>
                    <p class="c1"><span class="c2">The delivery shall then occur within a timeframe of three (3) months, starting with the date of appliance of the related Order Form. Any remaining amount of Training Services past those three months will be lost. Cancellation of Training Services are subject to eXo&rsquo;s Professional Services Cancellation Policy or specified in an applicable Order Form. Unless specified in the Order Form, additional fees such as transport, lodging, third party materials including software licenses, hardware, training, documentation needed to fulfill the delivery requirements are excluded from the scope of the delivery and shall be provided or ordered separately by the Customer. </span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">3. eXo Specific Development Services</span></p>
                    <p class="c1"><span class="c0">3.1. Scope of appliance</span></p>
                    <p class="c1"><span class="c10">eXo&rsquo;s specific development services (&ldquo;Specific Development Services&rdquo;) are meant to provide Customer on project development involving the Software such as :</span></p>
                    <ul class="c23 lst-kix_list_4-0 start">
                        <li class="c49"><span class="c10">Specific development,</span></li>
                        <li class="c49"><span class="c10">Custom extensions,</span></li>
                        <li class="c49"><span class="c10">Off-site PoCs (Proof of Concept),</span></li>
                    </ul>
                    <p class="c1"><span class="c2">The Specific Development Services don&rsquo;t include any Subscription Plan.</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c0">3.2. Performance and Deliverables</span></p>
                    <p class="c1"><span class="c10">eXo agrees to provide the Specific Development Services and Deliverables specified in an Order Form on the terms and conditions of this Appendix and in accordance with the requirements, Deliverables description and delivery dates according to the technical offer appendix provided by eXo with such Order Form. </span></p>
                    <p class="c1"><span class="c10">eXo and the Customer agree to scope the amount and nature of Specific Development Services needed in accordance with the involved project. </span></p>
                    <p class="c1"><span class="c10">Specific Development Services are generally tied to statement of work which will:</span></p>
                    <ul class="c23 lst-kix_list_5-0 start">
                        <li class="c49"><span class="c10">Specify the applicable documents or materials used to describe the Customer requirements and expected results</span></li>
                        <li class="c49"><span class="c10">Specify the eXo&rsquo;s project management phases, Deliverables, commitments and means to fulfill the project requirements and warrant the result.</span></li>
                    </ul>
                    <p class="c1" id="h.1fob9te"><span class="c10">Such an offer will be attached to an Order Form and is subject to changes emitted through a </span><span class="c3">&ldquo;Change Order&rdquo;</span><span class="c10">, as set forth below. </span></p>
                    <p class="c1"><span class="c3">Change Order. </span><span class="c10">The parties may, upon mutual agreement in a written order, at any time, make changes including deletions or additions, within the general scope of this Agreement, to the Specific Development Services to be performed. If any such change causes an increase or decrease in the time and/or means required for performance of any part of the Specific Development Services, the parties will make an equitable adjustment in delivery schedule and shall modify applicable Order Form accordingly. </span></p>
                    <p class="c1"><span class="c0">3.3. Ordering, invoicing and Payment</span></p>
                    <p class="c1"><span class="c10">eXo&rsquo;s Specific Development Services are meant to be ordered prior to project start and paid in compliance with the payment term or the payment schedule specified in the related Order Form.</span></p>
                    <p class="c1"><span class="c10">The Specific Development Services will be deemed definitively accepted upon &nbsp;validation by the Customer of the Deliverables provided by eXo in accordance with the delays, the scope and the expected quality criteria specified within the technical and organizational offer attached to the corresponding Order Form.</span></p>
                    <p class="c1"><span class="c10">Unless specified in the Order Form, additional fees such as transport, lodging, third party materials including software licenses, hardware, training, documentation needed to fulfill the delivery requirements are excluded from the scope of the delivery and shall be provided or ordered separately by the Customer.</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1"><span class="c0">4. eXo&rsquo;s Professional Services Cancellation Policy</span></p>
                    <p class="c73" id="h.3znysh7"><span class="c15">Training courses registrations or Professional Services resource allocations are not confirmed until an executed Order Form is received. </span></p>
                    <p class="c73" id="h.2et92p0"><span class="c15">Unless specified in an applicable Order Form all purchases of Professional Services are final, tied to a validity period and non-refundable.</span></p>
                    <p class="c73"><span class="c15">Customer may either reschedule or select credit toward a future allocation up to 14 (fourteen) calendar days prior to the start date of the Professional Services Customer purchased. The credit must be used within 3 months of the original course start date.</span></p>
                    <p class="c73"><span class="c15">eXo reserves the right to cancel any Training Services course. If a course is cancelled, we will contact students by telephone and email to arrange for training credit. Every effort will be made to reschedule a cancelled course or transfer enrollments to a later date.</span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c15"></span></p>
                    <p class="c113 c68"><span class="c0">Appendix 3: Hosting and Managed Services</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <ol class="c23 lst-kix_list_8-0 start" start="1">
                        <li class="c1 c46"><span class="c0">Hosting and Managed Services General Policy</span></li>
                    </ol>
                    <ol class="c23 lst-kix_list_8-1 start" start="1">
                        <li class="c1 c39"><span class="c0">Hosting and Managed Services entitlement</span></li>
                    </ol>
                    <p class="c1"><span class="c15">The Hosting and Managed Services are intended only for use by Customer (including through its contractors and agents) and for </span></p>
                    <p class="c1"><span class="c15">the benefit of the Customer and only for the Installed Systems (as defined below) for which Customer has purchased a Subscription Plan. Any unauthorized use of the Hosting and Managed Services will be deemed to be a material breach of this Agreement. In addition, usage of the Hosting and Managed Services is governed by the Fair Use policy as stated in chapter 1.5 of this appendix.</span></p>
                    <ol class="c23 lst-kix_list_8-1" start="2">
                        <li class="c1 c39"><span class="c13">Hosting and Managed Services Start Date</span></li>
                    </ol>
                    <p class="c1"><span class="c15">Unless otherwise agreed in an Order Form, the Hosting and Managed Services will begin on the date Customer purchases a Subscription Plan as set forth in the applicable Order Form.</span></p>
                    <ol class="c23 lst-kix_list_8-1" start="3">
                        <li class="c1 c39"><span class="c0">Hosting and Managed Services Scope of Coverage </span></li>
                    </ol>
                    <p class="c1"><span class="c2">Customer may elect to hand over the Production hosting and management of the Software to eXo to benefit from a SaaS (Software As A Service) deployment. Hosting and Managed Services serve this purpose and must be ordered as an optional feature of a Subscription Plan. </span></p>
                    <p class="c1"><span class="c2">Hosting and Managed Services consist of the provision of a tier-based set of features as defined in chapter 2 of this appendix.</span></p>
                    <p class="c1"><span class="c2">For the avoidance of doubt, Hosting and Managed Services exclude the following benefits and/or activities :</span></p>
                    <ul class="c23 lst-kix_list_11-0 start">
                        <li class="c1 c46"><span class="c10">Access to servers and/or any component of the hosting infrastructure (eg through SSH or any remote command line interface),</span></li>
                        <li class="c1 c46"><span class="c10">Customizations, Specific developments, Specific integrations (indicated as &ldquo;</span><span class="c3">Custom Extensions</span><span class="c10">&rdquo; hereafter) and their related testing, user acceptance, technical acceptance. For the avoidance of doubt, shall Customer require to deploy programmatic extensions on top of the Software, Customer will remain fully responsible of Custom Extensions quality and compliance with the Software development and operating guidelines. Such rules are available in the Documentation. eXo reserves full rights to undeploy Customer extensions which infringes either the the Security and Data Privacy or Fair Use policies.</span></li>
                        <li class="c1 c46"><span class="c10">Installation of Add-ons which are not eligible as an eXo Official Add-On as stated in Appendix 4,</span></li>
                    </ul>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <ol class="c23 lst-kix_list_8-1" start="4">
                        <li class="c1 c39"><span class="c0">Definitions</span></li>
                    </ol>
                    <p class="c1"><span class="c10">eXo Hosting is the hosting platform designed and used by eXo to deliver the Software as a service. For the purpose of providing the Service to Customer, Installed Systems hosting the Software Service are physically located on a server in data centers of partners specifically selected by eXo. eXo data center partners provide hardware infrastructure, power, network, storage and backup services. eXo is responsible for provisioning, monitoring, and managing the servers, and for providing Managed Services to eXo Hosting customers.</span></p>
                    <ol class="c23 lst-kix_list_8-1" start="5">
                        <li class="c1 c39"><span class="c0">Communications and Delivery</span></li>
                    </ol>
                    <p class="c1"><span class="c2">Unless otherwise set forth in an applicable Order Form, (1) eXo will be the primary source for communication with Customer and (2) The Managed Services entry point will be performed through the eXo Support Services desk and (3) the provision of the Hosting Services will be deemed accepted once Customer accesses the Service successfully. </span></p>
                    <p class="c1"><span class="c2">By entering this Agreement, Customer agrees to NOT establish direct contact toward a an eXo data center partners for any technical or business case that involves a Software installation Hosted and Managed by eXo.</span></p>
                    <ol class="c23 lst-kix_list_8-1" start="6">
                        <li class="c1 c39"><span class="c0">Fair Use Policy</span></li>
                    </ol>
                    <p class="c1"><span class="c2">eXo Hosting and Managed Services are delivered in exchange of a Fair Use Policy counterpart that introduces a set of obligations to Customer and Users. </span></p>
                    <ol class="c23 lst-kix_list_8-2 start" start="1">
                        <li class="c1 c41"><span class="c0">Access</span></li>
                    </ol>
                    <p class="c88"><span class="c15">Access to the Service is available at the Internet address provided by eXo upon initialization of the Service. Upon entering into this Agreement with the Customer or User, eXo will provide the Customer or User with a username and password for accessing the Service.</span></p>
                    <p class="c88"><span class="c15">eXo reserves the right to refuse registration, or to refuse or limit access to the Service, to anyone in its sole discretion. Customer will provide accurate, current, and complete information in connection with User registration and agree to maintain the security of their username(s) and password(s) at all times. CUSTOMER UNDERSTAND THAT ANY PERSON WITH A REGISTERED USER USERNAME(S) AND PASSWORD(S) MAY BE ABLE TO ACCESS THE CORRESPONDING USER ACCOUNT (INCLUDING ALL CONTENT THEREIN). CUSTOMER ACCEPTS ALL RISKS OF UNAUTHORIZED ACCESS TO A REGISTERED USER ACCOUNT BASED ON THE SHARING OR LOSS OF A USERNAME AND PASSWORD. Usernames and passwords are personal to each User, should be kept in confidence and may only used by the physical person to whom they are assigned. Customer are at all times fully liable for all acts and omissions by Users who have access to their account on the Service. Users have an access granted, and Customer agree to indemnify eXo for all claims and losses related to such acts and omissions. Customer will promptly notify eXo if it discovers or otherwise suspects any security breaches related to the Service, including any unauthorized use or disclosure of a username or password.</span></p>
                    <p class="c88"><span class="c15">Users are responsible for all activity occurring under their User or Customer account and shall abide by all applicable local, state, national and foreign laws, treaties and regulations in connection with their use of the Service, including those related to data privacy, international communications and the transmission of technical or personal data. In addition, User shall be responsible for abiding by any and all internal policies, procedures and regulations, which are required, by their employer and/or the applicable administrators of their account. Customer shall: (i) notify eXo immediately of any unauthorized use of any password or account or any other known or suspected breach of security; (ii) report to eXo immediately and use reasonable efforts to stop immediately any copying or distribution of content that is known or suspected by any User to violate this Agreement or the intellectual property rights of third parties; and (iii) not impersonate another Service user or provide false identity information to gain access to or use the Service. By accessing the Service, Customer represents and warrants that Users have not falsely identified themselves nor provided any false information to gain access to the Service and that Customer billing information, if any, is correct.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="2">
                        <li class="c1 c41"><span class="c0">Disruption</span></li>
                    </ol>
                    <p class="c1"><span class="c2">Users should not attempt activities that could cause any disruption such as :</span></p>
                    <p class="c31"><span class="c2">Compromising the integrity of our systems. This could include probing, scanning, or testing the vulnerability of any system or network that hosts our services.</span></p>
                    <p class="c31"><span class="c2">Tampering with, reverse-engineering, or hacking our services, circumventing any security or authentication measures, or attempting to gain unauthorized access to the services, related systems, networks, or data.</span></p>
                    <p class="c31"><span class="c2">Modifying, disabling, or compromising the integrity or performance of the services or related systems, network or data.</span></p>
                    <p class="c31"><span class="c2">Deciphering any transmissions to or from the servers running the services.</span></p>
                    <p class="c31"><span class="c2">Overwhelming or attempting to overwhelm our infrastructure by imposing an unreasonably large load on our systems that consume extraordinary resources (CPUs, memory, disk space, bandwidth, etc.), such as:</span></p>
                    <ul class="c23 lst-kix_list_10-0 start">
                        <li class="c73 c68 c46"><span class="c10">Using &ldquo;robots,&rdquo; &ldquo;spiders,&rdquo; &ldquo;offline readers,&rdquo; or other automated systems to sends more request messages to our servers than a human could reasonably send in the same period of time by using a normal browser</span></li>
                        <li class="c73 c68 c46"><span class="c10">Going far beyond the use parameters for any given service as described in its corresponding documentation</span></li>
                        <li class="c68 c46 c73"><span class="c10">Consuming an unreasonable amount of storage for music, videos, pornography, etc., in a way that&rsquo;s unrelated to the purposes for which the services were designed</span></li>
                    </ul>
                    <ol class="c23 lst-kix_list_8-2 start" start="1">
                        <li class="c77 c41 c68"><span class="c13">Wrongful Activities</span></li>
                    </ol>
                    <p class="c1"><span class="c2">Users should not attempt any wrongful activities such as :</span></p>
                    <p class="c31"><span class="c2">Misrepresentation of Users, or disguising the origin of any content (including by &ldquo;spoofing&rdquo;, &ldquo;phishing&rdquo;, manipulating headers or other identifiers, impersonating anyone else, or falsely implying any sponsorship or association with eXo or any third party)</span></p>
                    <p class="c31"><span class="c2">Using the services to violate the privacy of others, including publishing or posting other people&#39;s private and confidential information without their express permission, or collecting or gathering other people&rsquo;s personal information (including account names or information) from our services</span></p>
                    <p class="c31"><span class="c2">Using our services to stalk, harass, or post direct, specific threats of violence against others</span></p>
                    <p class="c31"><span class="c2">Using the Services for any illegal purpose, or in violation of any laws (including without limitation data, privacy, and export control laws)</span></p>
                    <p class="c31"><span class="c2">Accessing or searching any part of the services by any means other than our publicly supported interfaces (for example, &ldquo;scraping&rdquo;)</span></p>
                    <p class="c31"><span class="c2">Using meta tags or any other &ldquo;hidden text&rdquo; including eXo or our suppliers&rsquo; product names or trademarks</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="2">
                        <li class="c41 c68 c77"><span class="c6">Inappropriate Communications</span></li>
                    </ol>
                    <p class="c1"><span class="c2">Users should not attempt activities that could cause any inappropriate communications such as :</span></p>
                    <p class="c31"><span class="c2">Using the services to generate or send unsolicited communications, advertising, chain letters, or spam</span></p>
                    <p class="c31"><span class="c2">Soliciting our users for commercial purposes, unless expressly permitted by eXo</span></p>
                    <p class="c31"><span class="c2">Disparaging eXo or our partners, vendors, or affiliates</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="3">
                        <li class="c77 c41 c68"><span class="c6">Inappropriate Content</span></li>
                    </ol>
                    <p class="c31"><span class="c2">Users should not attempt any activity such as posting, uploading, sharing, submitting, or otherwise providing Content that:</span></p>
                    <ul class="c23 lst-kix_list_9-0 start">
                        <li class="c73 c68 c46"><span class="c10">Infringes eXo&rsquo;s or a third party&rsquo;s intellectual property or other rights, including any copyright, trademark, patent, trade secret, moral rights, privacy rights of publicity, or any other intellectual property right or proprietary or contractual right</span></li>
                        <li class="c73 c68 c46"><span class="c10">Users don&rsquo;t have the right to submit</span></li>
                        <li class="c73 c68 c46"><span class="c10">Is deceptive, fraudulent, illegal, obscene, defamatory, libelous, threatening, harmful to minors, pornographic (including child pornography), which we will remove and report to law enforcement, indecent, harassing, hateful</span></li>
                        <li class="c73 c68 c46"><span class="c10">Encourages illegal or tortious conduct or that is otherwise inappropriate</span></li>
                        <li class="c73 c68 c46"><span class="c10">Attacks others based on their race, ethnicity, national origin, religion, sex, gender, sexual orientation, disability, or medical condition</span></li>
                        <li class="c73 c68 c46"><span class="c10">Contains viruses, bots, worms, scripting exploits, or other similar materials</span></li>
                        <li class="c73 c68 c46"><span class="c10">Could otherwise cause damage to eXo or any third party</span></li>
                    </ul>
                    <p class="c108 c68"><span class="c2">&nbsp;</span></p>
                    <p class="c68 c108"><span class="c2">In this Fair Use Policy, the term &ldquo;Content&rdquo; means: (1) any information, data, text, software, code, scripts, music, sound, photos, graphics, videos, messages, tags, interactive features, or other materials that Users post, upload, share, submit, or otherwise provide in any manner to the services and (2) any other materials, content, or data Users provide to eXo or use with the Services.</span></p>
                    <p class="c73 c68"><span class="c2">Without affecting any other remedies available to eXo, eXo may permanently or temporarily terminate or suspend a user&rsquo;s account or access to the services without notice or liability if eXo (in its sole discretion) determines that a user has violated this Fair Use Policy.</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <ol class="c23 lst-kix_list_8-1" start="7">
                        <li class="c1 c39"><span class="c0">Security and Data Privacy Policy</span></li>
                    </ol>
                    <ol class="c23 lst-kix_list_8-2 start" start="1">
                        <li class="c1 c41"><span class="c0">Security Statement</span></li>
                    </ol>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">Data Storage</span></p>
                    <p class="c1"><span class="c2">eXo Hosting platform was designed and optimized by eXo specifically to host the Software and has multiple levels of redundancy built in. The applications themselves run on a separate front-end hardware node than that on which the data is stored. Hardware failure of the computing node is recovered by starting a new computing node. Application data is stored on storage nodes which have built-in redundancy to protect Customer data against equipment failure and to ensure data availability during datacenter maintenance events. </span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">Facilities</span></p>
                    <p class="c1"><span class="c2">Access to the data centers is limited to authorized personnel only as stated by our data center partners.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">People and Access</span></p>
                    <p class="c1"><span class="c2">Our global support team maintains an account on all cloud systems and applications for the purposes of maintenance and support. This support team accesses hosted applications and data only for purposes of application health monitoring and performing system or application maintenance, and upon customer request via our support system. Within eXo, only authorized eXo employees have access to application data. Authentication is done via individual encryption keys, rather than passwords, and the servers only accept incoming SSH connections from known locations. eXo Hosting is designed to allow application data to be accessible only with appropriate credentials, such that one customer cannot access another customer&#39;s data without explicit knowledge of that other customers&#39; credentials information. </span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">Monitoring</span></p>
                    <p class="c1"><span class="c2">The eXo operations team monitors the eXo Hosting platform on a 24x7x365 basis.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c0">Backups</span></p>
                    <p class="c1"><span class="c2">Application data backups for eXo Hosting occur on a daily basis ; Each backup is stored redundantly across multiple locations retained for a duration set forth in the benefits of Customer&rsquo;s Subscription Plan. All backup data is encrypted.</span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <ol class="c23 lst-kix_list_8-2" start="2">
                        <li class="c1 c41"><span class="c0">Data Privacy Policy</span></li>
                    </ol>
                    <p class="c1 c36"><span class="c0"></span></p>
                    <p class="c88"><span class="c69">eXo does not own any data, information or material that Users submit to the Service in the course of using the Service (</span><span class="c13">&quot;Uploaded Data&quot;</span><span class="c15">). Customer shall have sole responsibility for the accuracy, quality, integrity, legality, reliability, appropriateness, and intellectual property ownership or right to use any and all Uploaded Data that Users submit. eXo shall not be responsible or liable for the deletion, correction, destruction, damage, loss or failure to store any Uploaded Data.</span></p>
                    <p class="c88"><span class="c15">The Customer and/or User are fully liable for the legality of all Uploaded Data stored by the Customer and/or User on the Service. Furthermore, the Customer and/or User is fully liable if such Uploaded data is infringing upon third party rights, and accordingly agrees to indemnify eXo for all claims and judgements&#39; related to such infringement and/or illegality.</span></p>
                    <p class="c88"><span class="c15">If eXo on its own or through any third party has notice that Uploaded Data stored by the Customer and/or User is in violation of any law or infringes third party rights, eXo shall have the unfettered right to, without liability to the Customer or User, immediately suspend access to such data without prior notice to the User or Customer. The Customer and/or User may be notified by eXo of any such action under this Section, when reasonable and possible.</span></p>
                    <p class="c88"><span class="c69">The administrator shall have control over all applicable Uploaded Data submitted to the Service, and all Uploaded Data will be deemed to be owned by and the property of the applicable employer. Upon request by the applicable administrator, eXo may remove, modify, edit or otherwise alter any applicable Uploaded Data.</span></p>
                    <p class="c71"><span class="c6">Personal Information.</span></p>
                    <p class="c71"><span class="c15">Users have complete control over their personal information. In general, they can use our Service without providing eXo with any personal information. However, there are instances where we must have their personal information in order for us to grant them access to our Service or to assist us in providing support or additional services to them. This information may include registration data (their name, company, address, email address, phone number, title, etc.), information request data and response data (&ldquo;User Information&rdquo;).</span></p>
                    <p class="c71"><span class="c13">Use of User Information</span><span class="c15">.</span></p>
                    <p class="c71"><span class="c15">We intend to use User Information for purposes of supporting Customer&rsquo;s relationship with us by providing a Service that is suitable to Users needs and alerting Users to products and service offerings (&ldquo;eXo Offerings&rdquo;) as they become available. This User Information may be retained by eXo to verify compliance with the agreement between eXo and Customer, to keep track of the domains from which people visit us, to create a user profile to better serve Users, or to simply contact them either electronically or otherwise. If User decide that we should not use his personal User Information to contact him, please let us know and we will not use that information for such purpose. Do not submit any User Information if they are less than 18 years of age.</span></p>
                    <p class="c71"><span class="c6">Monitoring.</span></p>
                    <p class="c71"><span class="c15">Although we are not obligated to do so, we may review User communications with respect to the Service to determine whether they comply with our Terms and Conditions. eXo will not have any liability or responsibility for the content of any communications Users provide, or for any errors or violations of any laws or regulations by Users.</span></p>
                    <p class="c71"><span class="c6">Disclosure of User Information.</span></p>
                    <p class="c71"><span class="c15">eXo does not sell, trade or transfer User Information to third parties. However, we may share User Information with our business partners for marketing, advertising or product/service offering purposes. We may share User Information with third parties aggregated, non-personal information. Such information does not identify Users individually. We also disclose User Information if: we have their consent; OR we need to share it in order to provide them with the products and/or services they requested; OR we respond to a court order; OR they violate our Terms of Service or we otherwise suspect that users are engaged in fraudulent or illegal activities. Users may separately agree to provide their personal information to third parties that provide content for eXo Offerings, in order to access or use their products or services. If they agree to provide such information to these third parties, then User personal information will be subject to their privacy policies.</span></p>
                    <p class="c71"><span class="c6">Accuracy and Security.</span></p>
                    <p class="c71"><span class="c15">The accuracy and security of User Information is important to eXo. Currently, Users may review and request updates to their User Information retained by eXo. If they contact us to correct their User Information, we will attempt to correct such inaccuracies in a timely manner. eXo is concerned with the security of their User Information and is committed to taking reasonable steps to protect it from unauthorized access and use of that personal information. To that end, we put in place the appropriate physical, electronic and managerial policies and procedures designed to secure their personal User Information. We also continue to implement procedures to maintain accurate, complete and current User Information. No method of transmission over the Internet, or method of electronic storage, is one hundred percent secure. Therefore, while we strive to use commercially acceptable means to protect Users personal information, we cannot guarantee its absolute security.</span></p>
                    <p class="c71"><span class="c6">Usernames and Passwords.</span></p>
                    <p class="c71"><span class="c15">Access to certain content on our Service may be allowed under a written agreement between Customer and eXo, and may require a username or password issued by eXo. In some cases, failure to provide personal information may prevent User from accessing certain eXo Service &nbsp;containing certain confidential information. By accessing and using our protected and secured Service, Users agree to maintain the confidentiality of the username and password they selected to access such Service and consent to our Terms of Service.</span></p>
                    <p class="c71"><span class="c6">Cookies. We use &ldquo;cookies&rdquo;.</span></p>
                    <p class="c71"><span class="c15">A cookie is a small data file that a web site can transfer to a visitor&rsquo;s hard drive to keep records of the visits to such site. A cookie contains information such as User&rsquo;s username that helps us recognize the pages them have visited and improve future visits, but the only personal information a cookie can contain is the information that they provide themselves. A cookie cannot read data off User&rsquo;s hard drive or read cookie files created by other sites. Information stored in cookies may be encrypted, however, we do not store any credit card number in cookies. If they prefer not to accept a cookie, they can set their web browser to warn them before accepting cookies, or they can refuse all cookies by turning them off in their web browser. Please note that certain features of our Service may not be available once cookies are disabled.&nbsp;</span></p>
                    <p class="c71"><span class="c6">External Links.</span></p>
                    <p class="c71"><span class="c15">The Service may provide links to other third party web sites. Even if the third party is affiliated with eXo through a business partnership or otherwise, eXo is not responsible for the privacy policies or practices or the content of such external links. These links are provided to Users for convenience purposes only and Users access them at their own risk.</span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <ol class="c23 lst-kix_list_8-1" start="8">
                        <li class="c1 c39"><span class="c13">Definitions</span></li>
                    </ol>
                    <ol class="c23 lst-kix_list_8-2 start" start="1">
                        <li class="c1 c41"><span class="c6">Registered Users Limit</span></li>
                    </ol>
                    <p class="c1"><span class="c15">As long as Customer has purchased a Subscription Plan including an Hosting and Managed Services Option, eXo will provision a Service sized with technical characteristics according to the number of Registered Users set forth in an applicable Order Form.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="2">
                        <li class="c1 c41"><span class="c6">Allocated Disk Space</span></li>
                    </ol>
                    <p class="c1"><span class="c15">The Disk Space characteristic defines the average amount of data provision per User. Upon initialization of the Service, eXo will provision Customer with an amount of data storage space equals to the number of Registered Users purchased multiplied with the per user Disk Space allocation. Customer can benefit this Disk Space allocation as a whole, eg some users can use more Disk Space than the per user provision provided the total consumption does not exceed the total allocated Disk Space. Additional Disk Space can be purchased separately.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="3">
                        <li class="c1 c41"><span class="c6">Reserved Bandwidth</span></li>
                    </ol>
                    <p class="c1"><span class="c69">The Reserved Bandwidth characteristic defines the average amount of Internet reserved bandwidth per User. Upon initialization of the Service, eXo will provision Customer with an amount of monthly Reserved Bandwidth equals to the number of Registered Users purchased multiplied with the Reserved Bandwidth monthly allocation. Customer can benefit this Reserved Bandwidth monthly allocation as a whole, eg some users can use more Reserved Bandwidth than the per user provision provided the total consumption does not exceed the Reserved Bandwidth total monthly allocation. Additional Reserved Bandwidth can be purchased separately. Unused Monthly Reserved Bandwidth is not transferred to the next month.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="4">
                        <li class="c1 c41"><span class="c6">Backup and Restore Policy</span></li>
                    </ol>
                    <p class="c1"><span class="c15">The Backup Policy inserts a security net to Customer&rsquo;s Data and service health. In case of disaster deemed applicable to eXo Hosting and Managed Services responsibility, eXo will restore the latest available Backup at no cost for the Customer. Shall the disaster cause fall into the exclusion set forth in section 1.3 of this appendix or be deemed related to and infringement of the Fair Use Policy, eXo will charge Customer with a restoration fee.</span></p>
                    <p class="c1"><span class="c15">eXo will maintain a Data Backup job according to a Daily frequency, and will keep archived backups for as long as stated in this Appendix (see chapter 2) and/or in an applicable Order Form.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="5">
                        <li class="c1 c41"><span class="c6">Installed Systems Provision</span></li>
                    </ol>
                    <p class="c1"><span class="c15">Once Customer has purchased a Subscription Plan including an Hosting and Managed Services Option, eXo will provision an Software Production instance accessible by Customer over the Internet. Unless specified in an applicable Order form, eXo will not provide any additional Hosted environment.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="6">
                        <li class="c1 c41"><span class="c6">Data Center Location </span></li>
                    </ol>
                    <p class="c1"><span class="c15">For a smooth usage of the service, Customer may elect to choose the geographical location of the Data Center Hosting both the eXo Platform Service and Data close to its premises location. eXo will offer Customer to choose among a limited list of available locations. Some specific locations may induce additional Hosting, Reserved Bandwidth and Disk Space costs. Those additional costs will be reflected in an applicable Order Form. Upon choosing the Data Center Location, Customer will benefit from a provision of the Service by eXo. eXo warns Customer that some specific Data Center Locations may induce specificity according to local law. Customer agrees to not infringe those laws through the usage of the service. </span></p>
                    <p class="c1"><span class="c15">Changing Data Center Locations past the initial provisioning &nbsp;will induce an additional fee.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="7">
                        <li class="c1 c41"><span class="c6">Scheduled Maintenance Windows </span></li>
                    </ol>
                    <p class="c1"><span class="c15">eXo requires to perform regular maintenance operations over the Hosting service to ensure a proper user experience, secure data, stability and benefit from up-to-date features and bug fixes. Those maintenance operations are performed during predefined periods of time when the Service will be unavailable called the Scheduled Maintenance Windows. Customer may choose the preferred hour range for these Scheduled &nbsp;Maintenance Windows. &nbsp;Such maintenance may include (at the sole discretion of eXo) :</span></p>
                    <ul class="c23 lst-kix_list_7-0 start">
                        <li class="c1 c46"><span class="c69">Backups,</span></li>
                        <li class="c1 c46"><span class="c69">Software Updates,</span></li>
                        <li class="c1 c46"><span class="c69">Software Upgrades,</span></li>
                        <li class="c1 c46"><span class="c69">Any pro-active action necessary to ensure the System performance and security,</span></li>
                        <li class="c1 c46"><span class="c69">All On demand Tasks related operations that require a service interruption</span></li>
                    </ul>
                    <p class="c1"><span class="c15">Scheduled Maintenance Windows usage and expected duration will be notified by eXo to Customer prior to the maintenance operations.</span></p>
                    <p class="c1"><span class="c15">The Service is not accessible during Scheduled Maintenance Windows.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="8">
                        <li class="c1 c41"><span class="c6">Custom Domain</span></li>
                    </ol>
                    <p class="c1"><span class="c15">By default, and upon purchasing a Subscription Plan including an Hosting and Managed Services Option, Service will be accessible to Customer under a subdomain of a domain owned by eXo. Eligible Customers can provide a Custom Domain (either registered to an official domain registrar or already existing) to map to the Hosting server. For the avoidance of doubt, Customer will be responsible for purchasing a domain name to an external domain registrar and managing it. eXo will provide guidelines to Customer for the purpose of DNS configurations and Customer will provide to eXo a SSL certificate to setup secured accesses. The initial Service provision will not fully occur without those pre-requisites.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="9">
                        <li class="c1 c41"><span class="c6">Enterprise Directory or SSO Integration</span></li>
                    </ol>
                    <p class="c1"><span class="c15">Customer may be eligible to benefit from integrating his existing Enterprise Directory or Single Sign On service to the Software Service. eXo will set up such an integration. The nature of equipments, software and amount of work to setup this integration will be specified in an applicable Order Form and must be purchased separately by Customer.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="10">
                        <li class="c1 c41"><span class="c6">Service Level Objective</span></li>
                    </ol>
                    <p class="c1"><span class="c15">In addition to the SLA provided by Support Services, Customer who purchased a Subscription Plan including an Hosting and Managed Services option benefit from a specific indicator (SLO) regarding the availability of the Service and according to the selected tier (see Table in chapter 2). SLO calculations always exclude Scheduled Maintenance Windows.</span></p>
                    <ol class="c23 lst-kix_list_8-2" start="11">
                        <li class="c1 c41"><span class="c6">Guaranteed Restoration Time</span></li>
                    </ol>
                    <p class="c1"><span class="c15">In addition to the above, and according to the Hosting and Managed Services tier purchased by a customer through a Subscription plan, and in case of disaster Customer will benefit from Guaranteed Restoration Time of the Service. For the purpose of calculation of the effective Resolution Time, eXo will consider the clock start upon acknowledgment of the issuance of a severity 1 incident through eXo Support Services desk and the clock and as soon as the Service is restarted and the severity 1 is no longer applicable. (See Support Services SLA for acknowledgement guidelines).</span></p>
                    <p class="c1"><span class="c15">Customer accepts that the incident being downgraded to a lesser severity will constitute a success in the Guaranteed Resolution Time objective.</span></p>
                    <p class="c1"><span class="c15">Guaranteed Restoration Times exclude operations required to be performed during Scheduled &nbsp;Maintenance Windows.</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <ol class="c23 lst-kix_list_8-2" start="12">
                        <li class="c1 c41"><span class="c6">Automatic Software Version Updates</span></li>
                    </ol>
                    <p class="c1"><span class="c69">For the purpose of ensuring the best user experience through new functionalities, feature enhancements, security and bug fixes, eXo will regularly and automatically upgrade Customer&rsquo;s Installed systems with the latest Software version available. Software version management and lifecycle are available under the terms specified at <a href="https://www.exoplatform.com/maintenance-program">https://www.exoplatform.com/maintenance-program</a>.</span></p>
                    <p class="c1"><span class="c15">These operations are performed during a Scheduled Maintenance Window.</span></p>
                    <p class="c1"><span class="c15">Customer remains fully responsible of the compatibility of deployed Custom Extensions. For the purpose of sparing time for testing Custom Extensions toward a newer version, eXo will notify about Software Version Updates plans no less than 15 (fifteen) days before upgrading.</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <ol class="c23 lst-kix_list_8-2" start="13">
                        <li class="c1 c41"><span class="c6">On-demand Tasks Credit</span></li>
                    </ol>
                    <p class="c1"><span class="c15">Customer who purchased an Hosting and Managed Services option through a subscription plan benefit from a time credit for on-demand operational tasks to be performed by eXo. Such operations require an exceptional Scheduled Maintenance Window and have a predefined expected duration and associated Service Interruption Duration.</span></p>
                    <p class="c1"><span class="c15">On-demand tasks are generally requested through a Task Order request performed at eXo Support Services Desk and acknowledged by Customer and eXo before execution.</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <a id="t.0c60513aa36c698b0d46307ed7c426531a20a43f"></a>
                    <a id="t.5"></a>
                    <table class="c20">
                        <tbody>
                            <tr class="c55">
                                <td class="c14 c83" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Typical Tasks Related Service Operations</span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Expected Duration</span></p>
                                </td>
                                <td class="c45 c83" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Expected Service Interruption Duration</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">System Reset</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">2h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 4h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">System Restart</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">-</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 10 mins</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Software Maintenance Version Update</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">2h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 24h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Software Minor Version Update</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">4h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 24h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Custom Domain setup or change</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 24h (depending on DNS propagation)</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Data Backup Restoration</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">8h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 48h depending on the size of data to restore</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Data Export</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">8h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 48h depending on the size of data to export</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Service Tier upgrade</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">8h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 48h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Official Add-on Installation</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Official Add-on Update</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Custom Extension deployment</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1h</span></p>
                                </td>
                                <td class="c45" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Up to 8h</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c6"></span></p>
                    <p class="c1"><span class="c15">Additional On-demand tasks not appearing in the above table may still be performed but will require a dedicated quotation and credit through a Task Order emitted by eXo.</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <p class="c1 c36"><span class="c6"></span></p>
                    <ol class="c23 lst-kix_list_8-2" start="14">
                        <li class="c1 c41"><span class="c6">One-Off patches deployment</span></li>
                    </ol>
                    <p class="c1"><span class="c2">For the purpose of mitigating a particular defect or security issue, and according to eXo Support Services and maintenance program terms stated in Appendix 2, and according to the Hosting and Managed Services tier purchased by Customer, eXo may augment the Installed Systems deployment from time to time with One-Off patches. </span></p>
                    <p class="c1 c36"><span class="c2"></span></p>
                    <p class="c1"><span class="c3">2. Hosting and Managed Services Tiers</span></p>
                    <p class="c1"><span class="c15">As set forth in an applicable Order Form, purchasing a Subscription Plan entitles Customer to receive Hosting and Managed Services according to the following tiers (V means available, X means Not available for this tier) :</span></p>
                    <p class="c1 c36"><span class="c15"></span></p>
                    <a id="t.cfc2aa5ffa213fbf21c67aa53765369284c56445"></a>
                    <a id="t.6"></a>
                    <table class="c20">
                        <tbody>
                            <tr class="c5">
                                <td class="c116" colspan="4" rowspan="1">
                                    <p class="c4"><span class="c13">Hosting and Managed Services Tiers </span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c101" colspan="1" rowspan="1">
                                    <p class="c1 c36"><span class="c15"></span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Professional</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Enterprise</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Enterprise Plus</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Registered Users limit</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Specified in Order Form</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Specified in Order Form</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c72" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Service Features</span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c0"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c0"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c0"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Disk Space (per user)</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1 GB </span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">1 GB</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="2">
                                    <p class="c4"><span class="c15">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Reserved Bandwidth (per user per month)</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">500 MB </span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">500 MB</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Security and Data Privacy Policy</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c40"><span class="c2">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Backup and Restore Policy</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Daily backup with 7 days retention</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Daily backup &nbsp;with 1 month retention</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14 c83" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c6">Service Provisioning</span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c15"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c15"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c15"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">&nbsp;Installed Systems provision</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Production</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Production</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Datacenter location choice</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Europe, USA, Asia</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Europe, USA, Asia</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Europe, USA, Asia</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Scheduled Maintenance Window choice</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c59">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Custom domain </span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">X</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c59">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Custom extensions deployment</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">X</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Enterprise Directory or SSO Integration</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">X</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c72" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c13">Service Management</span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c15"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c2"></span></p>
                                </td>
                                <td class="c16 c83" colspan="1" rowspan="1">
                                    <p class="c4 c36"><span class="c2"></span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Service Level Objective</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">99,5% uptime</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">99,9% uptime</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="2">
                                    <p class="c4"><span class="c15">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c0">Guaranteed Restoration Time </span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">X</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">8h</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">Automatic Software Version update</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c40"><span class="c15">V</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">On-demand tasks credit (per year)</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">4h</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">8h</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">Specified in Order Form</span></p>
                                </td>
                            </tr>
                            <tr class="c5">
                                <td class="c14" colspan="1" rowspan="1">
                                    <p class="c8"><span class="c3">One-off patches</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">X</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c15">V</span></p>
                                </td>
                                <td class="c16" colspan="1" rowspan="1">
                                    <p class="c40"><span class="c15">V</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c26"><span class="c21"></span></p>
                    <p class="c26"><span class="c21"></span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c21"></span></p>
                    <p class="c26"><span class="c21"></span></p>
                    <hr style="page-break-before:always;display:none;">
                    <p class="c32 c68 c36"><span class="c21"></span></p>
                    <p class="c113 c68"><span class="c0">Appendix 4: eXo Official Add-ons</span></p>
                    <p class="c52 c36"><span class="c0"></span></p>
                    <p class="c52" id="h.tyjcwt"><span class="c10">The table below lists all supported add-ons for currently supported versions of the Software. Not all versions of every add-on is supported with every version of the Software. Compatibility matrix is available at <a href="https://www.exoplatform.com/supported-environments">https://www.exoplatform.com/supported-environments</a></span></p>
                    <p class="c52 c36"><span class="c2"></span></p>
                    <a id="t.e278257b5518c1fed146a42a8122fa6283665996"></a>
                    <a id="t.7"></a>
                    <table class="c20">
                        <tbody>
                            <tr class="c102">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Add-on name</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Introduces Specific Terms</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Required Subscription Plan</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c0">Eligible to Hosting and managed Option</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c3">Requires an additional subscription fee</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-chat - eXo Chat</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Professional, Enterprise or &nbsp;Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-cmis-addon - eXo CMIS Integration</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                            </tr>
                            <tr class="c85">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-crash-tomcat - CRaSH Add-on for Tomcat</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-crash-jboss - CRaSH Add-on for Jboss</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-ide-addon - eXo IDE Add-on</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c3">exo-video-calls - eXo Video Calls</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Yes (*)</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Professional, Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c10">Yes</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">exo-remote-edit - eXo Remote Edit</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Professional, Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">exo-cas - eXo CAS integration</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c119">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">exo-saml - eXo SAML2 integration</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">&nbsp;Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c78">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">exo-spnego - eXo SPNEGO integration</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c9" colspan="1" rowspan="1">
                                    <p class="c7"><span class="c0">exo-openam - eXo OpenAM integration</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c105 c81" colspan="1" rowspan="1">
                                    <p class="c111 c68 c120"><span class="c0">exo-tasks - eXo Tasks</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Professional, Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                            <tr class="c30">
                                <td class="c81 c105" colspan="1" rowspan="1">
                                    <p class="c68 c111"><span class="c0">exo-web-pack - eXo Web Pack</span></p>
                                </td>
                                <td class="c22" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Professional, Enterprise or Enterprise Plus</span></p>
                                </td>
                                <td class="c17" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">Yes</span></p>
                                </td>
                                <td class="c37" colspan="1" rowspan="1">
                                    <p class="c4"><span class="c2">No</span></p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p class="c1 c36"><span class="c21"></span></p>
                    <p class="c7"><span class="c2">(*) eXo Video Calls Add-on is governed and supported under the conditions of this Agreement and under the same Subscription Service Level as the Subscription Service Level applicable to the Software. For the avoidance of doubt and as long as both the Software Subscription and eXo Video Calls Add-on remain in force, eXo Video Calls Add-on will be deemed a part of the Software as defined by Section 1.9 of the Master Subscription Agreement.</span></p>
                    <p class="c32 c68 c36"><span class="c25"></span></p>
                    <p class="c26"><span class="c21"></span></p>

                    </div>
			<div class="bottom clearfix">
				<form name="tcForm" action="<%= contextPath + "/terms-and-conditions-action"%>" method="post">
					<div class="pull-left">
						<label class="uiCheckbox"><input type="checkbox" id="agreement" name="checktc" value="false" onclick="toggleState();" class="checkbox"/>
							<span>I agree with this terms and conditions agreement.</span>
						</label>
					</div>
					<div class="pull-right">
						<button class="btn inactive" disabled="disabled" id="continueButton" onclick="validate();">Continue</button>
					</div>
					<script type="text/javascript" src="<%=contextPath%>/javascript/welcomescreens.js"></script>
				</form>
			</div>
		</div>
	</body>
</html>
