/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#include <unidefs.h>
#if defined(SUN)
#include <unistd.h>
#endif

//https://stackoverflow.com/questions/7298762/send-email-with-c
#include <stdio.h>
#include <mailsender1705impl.hxx>
#include <iostream>
#include <fstream>
#include <string.h>
#include <stdlib.h>
#include <sstream>
#include <windows.h>
#include <winsock2.h>
#include <tc/preferences.h>


using namespace std;
using namespace VF98::Soa::CustomSrv::_2017_05;
using namespace Teamcenter::Soa::Server;

#pragma comment(lib, "ws2_32.lib")

// Insist on at least Winsock v1.1
#define VERSION_MAJOR 1
#define VERSION_MINOR 1

#define CRLF "\r\n"                 // carriage-return/line feed pair
#define endl "\n"

/**
 * configuration to mail server. i.e: ex.vingroup.net
 */
const char* KEY_VF_MAIL_SERVER = "vf_mail_server";

/**
 * configuration to mail server port. i.e: 25
 */
const char* KEY_VF_MAIL_SERVER_PORT = "vf_mail_server_port";

// Basic error checking for send() and recv() functions
bool Check(int iStatus, char *szFunction)
{
  if((iStatus != SOCKET_ERROR) && (iStatus)){
    return true;
  }

  cerr << "Error during call to " << szFunction << ": " << iStatus << " - " << GetLastError() << endl;

  return false;
}

	char* vf_mail_server =nullptr;
	int   vf_mail_server_port = -1;

int getTCMailPreferences() {

	int ret;

	char** vf_mail_server_pp = &vf_mail_server;

	ret = PREF_initialize();
	//ret |= PREF_set_search_scope(TC_preference_all);

	ret |= PREF_ask_char_value  ( KEY_VF_MAIL_SERVER, 0, vf_mail_server_pp);

	ret |= PREF_ask_int_value  ( KEY_VF_MAIL_SERVER_PORT, 0, &vf_mail_server_port);

	cout << "\nmail_server: "<< vf_mail_server << ":"<< vf_mail_server_port <<"\n";

	return ret;
}

static bool sendEmailRelayInternal(const std::string* mailFrom,
		const std::string* mailTo, const std::string* mailCC,
		const std::string* mailSubject, const std::string* mailContent,
		const std::string* mailAttachment) {

	(void) mailAttachment; //unused, should be added into mime at client side
	(void) mailSubject;
	(void) mailCC;

	  int         iProtocolPort        = 0;
	  char        szSmtpServerName[255] = "";
	  char        szToAddr[255]         = "";
	  char        szFromAddr[255]       = "";
	  static char        szBuffer[4096]       = "";
	  static char        szMsgLine[8192]       = "";

	  SOCKET      hServer = INVALID_SOCKET;

	  WSADATA     WSData;
	  LPHOSTENT   lpHostEntry;
	  SOCKADDR_IN SockAddr;

	  szMsgLine[0] ='\0';
	  szBuffer[0] ='\0';

	  if(mailFrom == nullptr) return false;
	  if(mailTo == nullptr) return false;
	  if(mailContent == nullptr) return false;

	  try {
		  if(getTCMailPreferences() != ITK_ok) return false;

		  // Load command-line args
		  lstrcpy(szSmtpServerName, vf_mail_server);
		  lstrcpy(szToAddr, mailTo->c_str());
		  lstrcpy(szFromAddr, mailFrom->c_str());

		  // Attempt to intialize WinSock (1.1 or later)
		  if(WSAStartup(MAKEWORD(VERSION_MAJOR, VERSION_MINOR), &WSData))
		  {
			cout << "Cannot find Winsock v" << VERSION_MAJOR << "." << VERSION_MINOR << " or later!" << endl;

			return 1;
		  }

		  // Lookup email server's IP address.
		  lpHostEntry = gethostbyname(szSmtpServerName);
		  if(!lpHostEntry)
		  {
			cout << "Cannot find SMTP mail server " << szSmtpServerName << endl;

			return false;
		  }

		  // Create a TCP/IP socket, no specific protocol
		  hServer = socket(PF_INET, SOCK_STREAM, 0);
		  if(hServer == INVALID_SOCKET)
		  {
			cout << "Cannot open mail server socket" << endl;

			return false;
		  }


		  // Use the SMTP default port if no other port is specified
			iProtocolPort = htons((u_short)vf_mail_server_port);

		  // Setup a Socket Address structure
		  SockAddr.sin_family = AF_INET;
		  SockAddr.sin_port   = (USHORT) iProtocolPort;
		  SockAddr.sin_addr   = *((LPIN_ADDR)*lpHostEntry->h_addr_list);

		  bool ret = false;
		  do {
			  // Connect the Socket
			  if(connect(hServer, (PSOCKADDR) &SockAddr, sizeof(SockAddr)))
			  {
				cout << "Error connecting to Server socket" << endl;
				break;
			  }

			  // Receive initial response from SMTP server
			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() Reply")){
				  break;
			  }

			  // Send HELO server.com
			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "HELO ");
			  strcat_s(szMsgLine, -1, szSmtpServerName);
			  strcat_s(szMsgLine, -1, CRLF);
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() HELO")) {
				  break;
			  }

			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() HELO")) {
				  break;
			  }

			  // Send MAIL FROM: <sender@mydomain.com>
			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "MAIL FROM:<");
			  strcat_s(szMsgLine, -1, szFromAddr);
			  strcat_s(szMsgLine, -1, ">\r\n" );
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() MAIL FROM")) {
				  break;
			  }
			   if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() MAIL FROM") ) {
				   break;
			   }

			  // Send RCPT TO: <receiver@domain.com>
			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "RCPT TO:<");
			  strcat_s(szMsgLine, -1, szToAddr);
			  strcat_s(szMsgLine, -1, ">\r\n" );
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() RCPT TO")) {
				  break;
			  }
			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() RCPT TO")) {
				  break;
			  }

			  // Send DATA

			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "DATA\r\n");
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() DATA")) {
				  break;
			  }
			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() DATA")) {
				  break;
			  }

			  const char* mime = mailContent->c_str();
			  if(!Check(send((unsigned int)hServer, (const char*) mime, (int) strlen(mime), 0),  "send() message-line")) {
				  break;
			  }

			  // Send blank line and a period
			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "\r\n.\r\n");
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() end-message")) {
				  break;
			  }
			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() end-message")) {
				  break;
			  }

			  // Send QUIT
			  szMsgLine[0] = '\0';
			  strcat_s(szMsgLine, -1, "\r\nQUIT\r\n");
			  if(!Check(send((unsigned int)hServer, (const char*) szMsgLine, (int) strlen(szMsgLine), 0), "send() QUIT")) {
				  break;
			  }
			  if(!Check(recv(hServer, szBuffer, sizeof(szBuffer), 0), "recv() QUIT")) {
				  break;
			  }

			  ret = true;

		  } while(0);

		  // Report message has been sent
		  //std::cout << "Sent " << " as email message to " << szToAddr << endl;

		  // Close server socket and prepare to exit.
		  if(hServer != INVALID_SOCKET)
			  closesocket(hServer);

		  WSACleanup();
	  } catch (...) {

		  cout << "ERR_VF_MAIL_SRV: an exception!" << endl;

		  if(hServer != INVALID_SOCKET)
		  {
			  closesocket(hServer);
		  }
	  }

	return true;
}

bool MailSenderImpl::sendEmail ( const std::string* mailFrom,
		const std::string* mailTo, const std::string* mailCC,
		const std::string* mailSubject, const std::string* mailContent,
		const std::string* mailAttachment )
{
	(void) mailSubject; //unused, should be added into mime at client side
	(void) mailAttachment; //unused, should be added into mime at client side
	(void) mailCC; //unused, should be added into mime at client side

	return sendEmailRelayInternal(mailFrom, mailTo,
			mailCC, mailSubject, mailContent, mailAttachment);

}


